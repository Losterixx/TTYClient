package dev.losterixx.ttyclient.client.screens

import com.google.gson.Gson
import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.screens.TTYClientTitleScreen.Companion.JETBRAINS_FONT
import dev.losterixx.ttyclient.client.ui.Draw
import dev.losterixx.ttyclient.client.ui.Theme
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import org.lwjgl.glfw.GLFW
import java.io.File

/**
 * This class contains much AI-generated code.
 */
class ConfigEditorScreen : Screen(Component.literal("Config Editor")) {


    private companion object {
        val ROOT: File = File(System.getProperty("user.home"), "TTYClient")
        val STATE_FILE = File(ROOT, ".editor-state.json")
        val STATE_GSON = Gson()

        const val DEFAULT_TREE_W = 145
        const val MIN_TREE_W = 60
        const val MAX_TREE_W = 400
        const val HDR_H = 18
        const val STATUS_H = 14

        val HEX_REGEX = Regex("\"(#[0-9A-Fa-f]{6}|#[0-9A-Fa-f]{8})\"")

        const val MODAL_W = 304
        const val MODAL_H = 100
        const val MODAL_BTN_W = 80
        const val MODAL_BTN_H = 18
        const val MODAL_BTN_GAP = 6

        const val SB_THICKNESS = 2
        const val SB_PADDING = 1

        fun GuiGraphicsExtractor.textWithFont(font: Font, text: String, x: Int, y: Int, color: Int, shadow: Boolean = false) {
            val component = Component.literal(text).setStyle(Style.EMPTY.withFont(JETBRAINS_FONT))
            this.text(font, component, x, y, color, shadow)
        }

        fun GuiGraphicsExtractor.textWithFont(font: Font, component: Component, x: Int, y: Int, color: Int, shadow: Boolean = false) {
            val styledComponent = component.copy().setStyle(component.style.withFont(JETBRAINS_FONT))
            this.text(font, styledComponent, x, y, color, shadow)
        }

        fun Font.widthWithFont(text: String): Int {
            val component = Component.literal(text).setStyle(Style.EMPTY.withFont(JETBRAINS_FONT))
            return this.width(component)
        }

        fun Font.widthWithFont(component: Component): Int {
            val styledComponent = component.copy().setStyle(component.style.withFont(JETBRAINS_FONT))
            return this.width(styledComponent)
        }
    }

    private data class Entry(val file: File, val depth: Int)
    private var tree: List<Entry> = emptyList()
    private val expanded = mutableSetOf(ROOT)
    private var treeScroll = 0
    private var treeFocusIdx = 0

    private var treeWidth = DEFAULT_TREE_W
    private var explorerVisible = true
    private val etw get() = if (explorerVisible) treeWidth else 0

    private var openFile: File? = null
    private var lines: MutableList<String> = mutableListOf()
    private var curLine = 0
    private var curCol = 0
    private var edScroll = 0
    private var edHScroll = 0
    private var dirty = false
    private var edFocus = false
    private var selAnchor: Pair<Int, Int>? = null
    private var dragging = false
    private var draggingVBar = false
    private var draggingHBar = false
    private var dragStartY = 0
    private var dragStartX = 0
    private var dragStartVScroll = 0
    private var dragStartHScroll = 0
    private var showUnsavedModal = false
    private var lastInputTime = 0L

    private data class Snap(val lines: List<String>, val curLine: Int, val curCol: Int, val selAnchor: Pair<Int, Int>?)
    private val undoStack = ArrayDeque<Snap>()
    private val redoStack = ArrayDeque<Snap>()

    private val undoKey get() = if (ConfigManager.general.keyBoard.isQwertz) GLFW.GLFW_KEY_Y else GLFW.GLFW_KEY_Z
    private val redoKey get() = if (ConfigManager.general.keyBoard.isQwertz) GLFW.GLFW_KEY_Z else GLFW.GLFW_KEY_Y

    private data class EditorState(
        val openFilePath: String? = null,
        val expandedPaths: Set<String> = emptySet(),
        val treeWidth: Int = DEFAULT_TREE_W,
        val explorerVisible: Boolean = true
    )

    private fun saveState() {
        val state = EditorState(
            openFilePath = openFile?.absolutePath,
            expandedPaths = expanded.map { it.absolutePath }.toSet(),
            treeWidth = treeWidth,
            explorerVisible = explorerVisible
        )
        try { STATE_FILE.writeText(STATE_GSON.toJson(state)) } catch (_: Exception) {}
    }

    private fun loadState() {
        if (!STATE_FILE.exists()) return
        try {
            val state = STATE_GSON.fromJson(STATE_FILE.readText(), EditorState::class.java) ?: return

            expanded.clear()
            expanded.add(ROOT)
            state.expandedPaths.forEach { path ->
                val f = File(path)
                if (f.exists() && f.isDirectory) expanded.add(f)
            }
            rebuild()

            treeWidth = state.treeWidth.coerceIn(MIN_TREE_W, MAX_TREE_W)
            explorerVisible = state.explorerVisible

            state.openFilePath?.let { path ->
                val f = File(path)
                if (f.exists() && f.isFile) openFile(f)
            }
        } catch (_: Exception) {}
    }

    override fun init() { ROOT.mkdirs(); rebuild(); loadState() }

    private fun rebuild() { tree = scan(ROOT, 0) }

    private fun scan(dir: File, depth: Int): List<Entry> =
        (dir.listFiles() ?: emptyArray())
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            .flatMap { f -> buildList {
                add(Entry(f, depth))
                if (f.isDirectory && f in expanded) addAll(scan(f, depth + 1))
            } }

    private fun openFile(file: File) {
        openFile = file

        lines = try { file.readLines().toMutableList().ifEmpty { mutableListOf("") } }
                catch (_: Exception) { mutableListOf("") }

        curLine = 0; curCol = 0; edScroll = 0; edHScroll = 0; dirty = false; edFocus = true
        selAnchor = null; undoStack.clear(); redoStack.clear()

        val idx = tree.indexOfFirst { it.file == file }
        if (idx >= 0) treeFocusIdx = idx
    }

    private fun save() {
        openFile?.let { f -> try { f.writeText(lines.joinToString("\n")); dirty = false } catch (_: Exception) {} }
    }

    private fun snap() = Snap(lines.toList(), curLine, curCol, selAnchor)

    private fun pushUndo() {
        undoStack.addLast(snap())
        if (undoStack.size > 200) undoStack.removeFirst()
        redoStack.clear()
    }

    private fun restore(s: Snap) { lines = s.lines.toMutableList(); curLine = s.curLine; curCol = s.curCol; selAnchor = s.selAnchor; dirty = true }
    private fun undo() { if (undoStack.isEmpty()) return; redoStack.addLast(snap()); restore(undoStack.removeLast()) }
    private fun redo() { if (redoStack.isEmpty()) return; undoStack.addLast(snap()); restore(redoStack.removeLast()) }

    private fun hasSel() = selAnchor != null && (selAnchor!!.first != curLine || selAnchor!!.second != curCol)

    private fun selRange(): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val a = selAnchor ?: return (curLine to curCol) to (curLine to curCol)
        val c = curLine to curCol

        return if (a.first < c.first || (a.first == c.first && a.second <= c.second)) a to c else c to a
    }

    private fun selText(): String {
        if (!hasSel()) return ""

        val (s, e) = selRange()
        if (s.first == e.first) return lines[s.first].substring(s.second, e.second)

        return buildString {
            append(lines[s.first].drop(s.second))
            for (i in s.first + 1 until e.first) { append('\n'); append(lines[i]) }
            append('\n'); append(lines[e.first].take(e.second))
        }
    }

    private fun delSelNoUndo() {
        if (!hasSel()) return

        val (s, e) = selRange()
        if (s.first == e.first) {
            lines[s.first] = lines[s.first].removeRange(s.second, e.second)
        } else {
            val tail = lines[e.first].drop(e.second)
            for (i in e.first downTo s.first + 1) lines.removeAt(i)
            lines[s.first] = lines[s.first].take(s.second) + tail
        }

        curLine = s.first; curCol = s.second; selAnchor = null
    }

    private fun getClip() = GLFW.glfwGetClipboardString(minecraft.window.handle()) ?: ""
    private fun setClip(t: String) = GLFW.glfwSetClipboardString(minecraft.window.handle(), t)

    private fun Char.isWord() = isLetterOrDigit() || this == '_'

    private fun moveWordLeft() {
        if (curCol == 0) { if (curLine > 0) { curLine--; curCol = lines[curLine].length }; return }

        val ln = lines[curLine]; var c = curCol
        while (c > 0 && ln[c - 1].isWhitespace()) c--
        if (c > 0 && ln[c - 1].isWord()) while (c > 0 && ln[c - 1].isWord()) c-- else if (c > 0) c--

        curCol = c
    }

    private fun moveWordRight() {
        val ln = lines[curLine]

        if (curCol == ln.length) { if (curLine < lines.size - 1) { curLine++; curCol = 0 }; return }

        var c = curCol
        when {
            ln[c].isWord() -> while (c < ln.length && ln[c].isWord()) c++
            ln[c].isWhitespace() -> while (c < ln.length && ln[c].isWhitespace()) c++
            else -> c++
        }

        curCol = c
    }

    private fun insert(s: String) {
        pushUndo()

        if (hasSel()) delSelNoUndo()

        val ln = lines[curLine]
        lines[curLine] = ln.take(curCol) + s + ln.drop(curCol)

        curCol += s.length; dirty = true; clamp(); scroll()
    }

    override fun extractRenderState(ctx: GuiGraphicsExtractor, mx: Int, my: Int, delta: Float) {
        val tr = font; val lh = tr.lineHeight + 3
        val cy1 = HDR_H; val cy2 = height - STATUS_H; val ch = cy2 - cy1

        Draw.rect(ctx, 0, 0, width, height, Theme.editorTheme.base)

        Draw.rect(ctx, 0, 0, etw, HDR_H, Theme.editorTheme.crust)
        Draw.rect(ctx, 0, HDR_H - 1, etw, 1, Theme.editorTheme.overlay)
        val expLabel = "File Manager"
        if (explorerVisible) ctx.textWithFont(tr, expLabel, (etw - tr.widthWithFont(expLabel)) / 2, (HDR_H - tr.lineHeight) / 2, Theme.editorTheme.treeOther, false)

        Draw.rect(ctx, etw + 1, 0, width - etw - 1, HDR_H, Theme.editorTheme.mantle)
        Draw.rect(ctx, etw + 1, HDR_H - 1, width - etw - 1, 1, Theme.editorTheme.overlay)
        val hdr = (openFile?.name ?: "Config Editor") + if (dirty) " ●" else ""
        ctx.textWithFont(tr, hdr, etw + 1 + (width - etw - 1 - tr.widthWithFont(hdr)) / 2, (HDR_H - tr.lineHeight) / 2, Theme.editorTheme.text, false)

        Draw.rect(ctx, 0, cy2, width, STATUS_H, Theme.editorTheme.mantle)
        Draw.rect(ctx, 0, cy2, width, 1, Theme.editorTheme.overlay)
        val selInfo = if (hasSel()) "  ${selText().length} sel  ·  " else "  "
        val st = if (openFile != null) "${selInfo}${if (dirty) "\uF444 unsaved" else "\uF00C saved"}   Ln ${curLine+1}, Col ${curCol+1}   [Ctrl+S] save·[Esc] close"
                else "  [Alt+↑/↓] navigate · [Alt+Enter] open · [Alt+Shift+←/→] resize tree · [Alt+Shift+↑/↓] toggle tree"
        ctx.textWithFont(tr, st, 4, cy2 + (STATUS_H - tr.lineHeight) / 2 + 2, Theme.editorTheme.comment, false)

        if (explorerVisible) Draw.rect(ctx, etw, 0, 1, height - 14, Theme.editorTheme.overlay)

        if (explorerVisible) {
            ctx.enableScissor(0, cy1, etw, cy2)
            drawTree(ctx, tr, lh, cy1, ch, mx, my)
            ctx.disableScissor()
        }

        ctx.enableScissor(etw + 1, cy1, width, cy2)
        drawEditor(ctx, tr, lh, cy1, ch)
        drawEdVBar(ctx, cy1, cy2)
        drawEdHBar(ctx, cy1, cy2)
        ctx.disableScissor()

        if (showUnsavedModal) drawUnsavedModal(ctx, mx, my)
    }

    private fun drawTree(ctx: GuiGraphicsExtractor, tr: Font, lh: Int, y0: Int, aH: Int, mx: Int, my: Int) {
        Draw.rect(ctx, 0, y0, treeWidth, aH, Theme.editorTheme.crust)

        val rootY = y0 - treeScroll + 3
        if (rootY + lh > y0 && rootY < y0 + aH) ctx.textWithFont(tr, "~/TTYClient", 3, rootY + (lh - tr.lineHeight) / 2, Theme.editorTheme.treeDir, false)

        tree.forEachIndexed { i, e ->
            val ey = y0 + (i + 1) * lh - treeScroll + 3
            if (ey + lh < y0 || ey > y0 + aH) return@forEachIndexed

            val sel = e.file == openFile
            val keyFocus = i == treeFocusIdx
            val hov = mx in 0 until treeWidth && my in ey until ey + lh

            when {
                sel -> { Draw.rect(ctx, 0, ey - 1, treeWidth, lh, Draw.withAlpha(Theme.editorTheme.surface, 120)); Draw.rect(ctx, 0, ey - 1, 2, lh, Theme.editorTheme.selection) }
                hov -> Draw.rect(ctx, 0, ey - 1, treeWidth, lh, Draw.withAlpha(Theme.editorTheme.surface, 70))
                keyFocus -> Draw.rect(ctx, 0, ey - 1, treeWidth, lh, Draw.withAlpha(Theme.editorTheme.surface, 50))
            }

            val indent = e.depth * 7
            if (e.depth > 0 && !e.file.isDirectory) {
                val guideX = 10 + (e.depth - 1) * 7
                Draw.rect(ctx, guideX, ey + 3, 1, lh / 2 - 4, Draw.withAlpha(Theme.editorTheme.overlay, 100))
                Draw.rect(ctx, guideX, ey + lh / 2 - 1, 4, 1, Draw.withAlpha(Theme.editorTheme.overlay, 100))
            }

            val icon = if (e.file.isDirectory) (if (e.file in expanded) "▾ " else "▸ ") else "  "
            val col = when {
                sel -> Theme.textPrimary
                e.file.isDirectory -> Theme.editorTheme.treeDir
                e.file.extension in listOf("jsonc", "json") -> Theme.editorTheme.treeJsonc
                e.file.extension == "txt" -> Theme.editorTheme.treeTxt
                else -> Theme.editorTheme.treeOther
            }

            ctx.textWithFont(tr, "$icon${e.file.name}", 4 + indent, ey + (lh - tr.lineHeight) / 2, col, false)
        }
    }

    private fun drawEditor(ctx: GuiGraphicsExtractor, tr: Font, lh: Int, y0: Int, aH: Int) {
        val ex = etw + 1
        Draw.rect(ctx, ex, y0, width - ex, aH, Theme.editorTheme.base)

        if (openFile == null) {
            val title = Component.literal("TTYClient Config Editor").withStyle(Style.EMPTY.withBold(true))
            ctx.textWithFont(tr, title, ex + (width - ex - tr.widthWithFont(title)) / 2, y0 + (aH - tr.lineHeight) / 2 - 20, Theme.editorTheme.comment, false)
            val msg = "Select a file from the file manager to edit it"
            ctx.textWithFont(tr, msg, ex + (width - ex - tr.widthWithFont(msg)) / 2, y0 + (aH - tr.lineHeight) / 2, Theme.editorTheme.comment, false)
            return
        }

        val gutterW = tr.widthWithFont("0".repeat(lines.size.toString().length)) + 8
        val codeX = ex + gutterW
        val textX = codeX + 2 - edHScroll

        val vis = aH / lh + 2
        for (i in 0 until vis) {
            val li = edScroll + i; if (li >= lines.size) break
            val ey = y0 + i * lh; val raw = lines[li]

            if (li == curLine && edFocus) Draw.rect(ctx, codeX + 1, ey, width - codeX - 1, lh, Draw.withAlpha(Theme.editorTheme.surface, 100))

            if (hasSel()) drawSelOnLine(ctx, tr, ey, lh, textX, raw, li)

            renderLine(ctx, tr, raw, textX, ey, lh)

            if (li == curLine && edFocus) {
                val now = System.currentTimeMillis()
                if (now - lastInputTime < 500 || now % 1000 > 400) {
                    val cx = colToVisualX(tr, raw, curCol, textX)
                    Draw.rect(ctx, cx, ey + 1, 1, tr.lineHeight, Theme.editorTheme.text)
                }
            }
        }

        Draw.rect(ctx, ex, y0, gutterW, aH, Theme.editorTheme.mantle)
        for (i in 0 until vis) {
            val li = edScroll + i; if (li >= lines.size) break
            val ey = y0 + i * lh
            val ns = "${li + 1}"
            ctx.textWithFont(tr, ns, ex + gutterW - tr.widthWithFont(ns) - 4, ey + (lh - tr.lineHeight) / 2, if (li == curLine) Theme.editorTheme.gutterActive else Theme.editorTheme.gutter, false)
        }

        Draw.rect(ctx, ex + gutterW, y0, 1, aH, Theme.editorTheme.overlay)
    }

    private fun drawEdVBar(ctx: GuiGraphicsExtractor, cy1: Int, cy2: Int) {
        if (openFile == null) return
        val lh = font.lineHeight + 3
        val trackX = width - SB_PADDING - SB_THICKNESS
        val trackY = cy1 + SB_PADDING
        val trackH = cy2 - cy1 - 2 * SB_PADDING
        Draw.rect(ctx, trackX, trackY, SB_THICKNESS, trackH, Draw.withAlpha(Theme.editorTheme.overlay, 70))
        val total   = lines.size.coerceAtLeast(1)
        val visible = ((cy2 - cy1) / lh).coerceAtLeast(1)
        val maxScr = (total - visible).coerceAtLeast(1)
        val thumbH = (trackH * visible.toFloat() / total).toInt().coerceAtLeast(8).coerceAtMost(trackH)
        val thumbY = trackY + ((trackH - thumbH).toFloat() * edScroll / maxScr).toInt()
        Draw.rect(ctx, trackX, thumbY.coerceIn(trackY, trackY + trackH - thumbH), SB_THICKNESS, thumbH, Theme.editorTheme.subtext)
    }

    private fun drawEdHBar(ctx: GuiGraphicsExtractor, cy1: Int, cy2: Int) {
        if (openFile == null) return

        val gutterW = font.widthWithFont("0".repeat(lines.size.toString().length)) + 8
        val codeStart = etw + 1 + gutterW + 1
        val codeEnd = width - SB_PADDING - SB_THICKNESS
        val codeAreaW = (codeEnd - codeStart).coerceAtLeast(1)
        val maxW = (lines.maxOfOrNull { font.widthWithFont(it) + 4 } ?: 0)
        val maxHScr = (maxW - codeAreaW).coerceAtLeast(0)
        val trackX = etw + 1 + SB_PADDING
        val trackY = cy2 - SB_PADDING - SB_THICKNESS
        val trackW = codeEnd - SB_PADDING - trackX

        if (trackW <= 0) return

        Draw.rect(ctx, trackX, trackY, trackW, SB_THICKNESS, Draw.withAlpha(Theme.editorTheme.overlay, 70))

        if (maxHScr == 0) return

        val thumbW = (trackW * codeAreaW.toFloat() / maxW).toInt().coerceAtLeast(8).coerceAtMost(trackW)
        val thumbX = trackX + ((trackW - thumbW).toFloat() * edHScroll / maxHScr).toInt()
        Draw.rect(ctx, thumbX.coerceIn(trackX, trackX + trackW - thumbW), trackY, thumbW, SB_THICKNESS, Theme.editorTheme.subtext)
    }

    private fun drawSelOnLine(ctx: GuiGraphicsExtractor, tr: Font, ey: Int, lh: Int, codeStart: Int, raw: String, li: Int) {
        val (s, e) = selRange()

        if (li < s.first || li > e.first) return
        val col1 = (if (li == s.first) s.second else 0).coerceAtMost(raw.length)
        val col2 = (if (li == e.first) e.second else raw.length).coerceAtMost(raw.length)
        val px1 = colToVisualX(tr, raw, col1, codeStart)
        val px2 = colToVisualX(tr, raw, col2, codeStart)
        val fullLine = li < e.first && !(li == e.first - 1 && e.second == 0)
        val w = if (fullLine) px2 - px1 + 4 else (px2 - px1).coerceAtLeast(0)

        if (w > 0) Draw.rect(ctx, px1, ey, w, lh, Draw.withAlpha(Theme.editorTheme.selection, 50))
    }

    private fun rgb(r: Int): TextColor = TextColor.fromRgb(r)

    private fun colorizedText(raw: String): Component {
        val t = raw.trimStart()
        if (t.startsWith("//") || t.startsWith("/*") || t.startsWith("*"))
            return Component.literal(raw).withStyle { it.withColor(rgb(Theme.editorTheme.comment)) }

        val root = Component.literal("")
        var i = 0

        fun add(s: String, c: Int) { if (s.isNotEmpty()) root.append(Component.literal(s).withStyle { it.withColor(rgb(c)) }) }

        while (i < raw.length) when {
            raw[i] == '"' -> {
                val start = i; i++
                while (i < raw.length) { if (raw[i] == '\\') { i += 2; continue }; if (raw[i] == '"') { i++; break }; i++ }
                val str   = raw.substring(start, i.coerceAtMost(raw.length))
                val isKey = raw.substring(i.coerceAtMost(raw.length)).trimStart().startsWith(':')
                add(str, if (isKey) Theme.editorTheme.key else Theme.editorTheme.string)
            }

            raw[i] == '/' && i + 1 < raw.length && raw[i + 1] == '/' -> {
                add(raw.substring(i), Theme.editorTheme.comment); return root
            }

            raw[i] in "{},:[" || raw[i] == ']' -> { add("${raw[i]}", Theme.editorTheme.punctuation); i++ }

            listOf("true", "false", "null").firstOrNull { raw.substring(i).startsWith(it) } != null -> {
                val kw = listOf("true", "false", "null").first { raw.substring(i).startsWith(it) }
                add(kw, Theme.editorTheme.keyword); i += kw.length
            }

            raw[i].isDigit() || (raw[i] == '-' && i + 1 < raw.length && raw[i + 1].isDigit()) -> {
                val start = i
                while (i < raw.length && (raw[i].isDigit() || raw[i] == '.' || (i == start && raw[i] == '-'))) i++
                add(raw.substring(start, i), Theme.editorTheme.number)
            }

            else -> { add("${raw[i]}", Theme.editorTheme.subtext); i++ }
        }

        return root
    }

    private fun hexArgb(hex: String): Int {
        val h = hex.removePrefix("#")
        return if (h.length == 6) 0xFF000000.toInt() or h.toInt(16) else h.toLong(16).toInt()
    }

    private fun renderLine(ctx: GuiGraphicsExtractor, tr: Font, raw: String, startX: Int, ey: Int, lh: Int) {
        val textY   = ey + (lh - tr.lineHeight) / 2
        val matches = HEX_REGEX.findAll(raw).toList()
        if (matches.isEmpty()) { ctx.textWithFont(tr, colorizedText(raw), startX, textY, Theme.editorTheme.subtext, false); return }

        val ss = tr.lineHeight - 2
        var x = startX
        var pos = 0

        for (m in matches) {
            if (m.range.first > pos) {
                val seg = raw.substring(pos, m.range.first)
                ctx.textWithFont(tr, colorizedText(seg), x, textY, Theme.editorTheme.subtext, false)
                x += tr.widthWithFont(seg)
            }

            ctx.textWithFont(tr, Component.literal("\"").withStyle { it.withColor(rgb(Theme.editorTheme.string)) }, x, textY, Theme.editorTheme.subtext, false)
            x += tr.widthWithFont("\"") + 1

            val argb = hexArgb(m.groupValues[1])
            Draw.rect(ctx, x, ey + 1, ss, ss, argb)
            x += ss + 2

            val tail = m.value.drop(1)
            ctx.textWithFont(tr, Component.literal(tail).withStyle { it.withColor(rgb(Theme.editorTheme.string)) }, x, textY, Theme.editorTheme.subtext, false)
            x += tr.widthWithFont(tail)

            pos = m.range.last + 1
        }

        if (pos < raw.length) ctx.textWithFont(tr, colorizedText(raw.substring(pos)), x, textY, Theme.editorTheme.subtext, false)
    }

    private fun visualXToCol(tr: Font, raw: String, mx: Int, startX: Int): Int {
        if (raw.isEmpty()) return 0

        for (i in raw.indices) {
            val xLeft = colToVisualX(tr, raw, i, startX)
            val xRight = colToVisualX(tr, raw, i + 1, startX)
            if (mx < xLeft + (xRight - xLeft) / 2) return i
        }

        return raw.length
    }

    private fun colToVisualX(tr: Font, raw: String, col: Int, startX: Int): Int {
        val ss = tr.lineHeight - 2
        val matches = HEX_REGEX.findAll(raw).toList()
        if (matches.isEmpty()) return startX + tr.widthWithFont(raw.take(col))

        var x = startX
        var pos = 0

        for (m in matches) {
            if (col <= m.range.first) return x + tr.widthWithFont(raw.substring(pos, col))
            x += tr.widthWithFont(raw.substring(pos, m.range.first + 1))
            x += ss + 2

            if (col <= m.range.last + 1) return x + tr.widthWithFont(raw.substring(m.range.first + 1, col))
            x += tr.widthWithFont(m.value.drop(1))
            pos = m.range.last + 1
        }

        return x + tr.widthWithFont(raw.substring(pos, col))
    }

    private fun saveAndClose() {
        save()
        showUnsavedModal = false
        minecraft.player?.sendOverlayMessage(Component.literal(MainClient.PREFIX + "§7Saved changes."))
        onClose()
    }

    private fun resetAndClose() {
        dirty = false
        showUnsavedModal = false
        minecraft.player?.sendOverlayMessage(Component.literal(MainClient.PREFIX + "§7Changes discarded."))
        onClose()
    }

    private fun cancelModal() {
        showUnsavedModal = false
    }

    private fun drawUnsavedModal(ctx: GuiGraphicsExtractor, mx: Int, my: Int) {
        Draw.rect(ctx, 0, 0, width, height, Draw.withAlpha(0x000000, 160))

        val modalX = (width  - MODAL_W) / 2
        val modalY = (height - MODAL_H) / 2

        Draw.roundedRect(ctx, modalX, modalY, MODAL_W, MODAL_H, 8, Theme.editorTheme.crust)
        Draw.roundedRectOutline(ctx, modalX, modalY, MODAL_W, MODAL_H, 8, Theme.editorTheme.overlay)

        val tr = font

        val title = "\uF071 Unsaved Changes \uF071"
        ctx.textWithFont(tr, title, modalX + (MODAL_W - tr.widthWithFont(title)) / 2, modalY + 10, 0xFFFFAA00.toInt(), false)

        val msg = "You have unsaved changes. What would you like to do?"
        ctx.textWithFont(tr, msg, modalX + (MODAL_W - tr.widthWithFont(msg)) / 2, modalY + 28, Theme.editorTheme.subtext, false)

        val hint = "Use keys [S]·[C]·[R] or click a button"
        ctx.textWithFont(tr, hint, modalX + (MODAL_W - tr.widthWithFont(hint)) / 2, modalY + 40, Theme.editorTheme.comment, false)

        val totalBtnW = MODAL_BTN_W * 3 + MODAL_BTN_GAP * 2
        val bX = modalX + (MODAL_W - totalBtnW) / 2
        val bY = modalY + MODAL_H - MODAL_BTN_H - 12

        val sX = bX
        val sHov = mx in sX until sX + MODAL_BTN_W && my in bY until bY + MODAL_BTN_H
        Draw.roundedRect(ctx, sX, bY, MODAL_BTN_W, MODAL_BTN_H, 4, if (sHov) Draw.withAlpha(Theme.editorTheme.surface, 100) else Draw.withAlpha(Theme.editorTheme.mantle, 180))

        val sLbl = "[S] Save"
        val slw = tr.widthWithFont(sLbl)
        val slx = sX + (MODAL_BTN_W - slw) / 2
        val sly = bY + (MODAL_BTN_H - tr.lineHeight) / 2
        ctx.textWithFont(tr, sLbl, slx, sly, Theme.textSecondary, false)
        Draw.rect(ctx, slx - 7, sly + tr.lineHeight + 4, slw + 14, 1, 0xFF4CAF50.toInt())

        val cX = bX + MODAL_BTN_W + MODAL_BTN_GAP
        val cHov = mx in cX until cX + MODAL_BTN_W && my in bY until bY + MODAL_BTN_H
        Draw.roundedRect(ctx, cX, bY, MODAL_BTN_W, MODAL_BTN_H, 4, if (cHov) Draw.withAlpha(Theme.editorTheme.surface, 100) else Draw.withAlpha(Theme.editorTheme.mantle, 180))

        val cLbl = "[C] Cancel"
        val clw = tr.widthWithFont(cLbl)
        val clx = cX + (MODAL_BTN_W - clw) / 2
        val cly = bY + (MODAL_BTN_H - tr.lineHeight) / 2
        ctx.textWithFont(tr, cLbl, clx, cly, Theme.textSecondary, false)
        Draw.rect(ctx, clx - 7, cly + tr.lineHeight + 4, clw + 14, 1, Theme.editorTheme.overlay)

        val rX = cX + MODAL_BTN_W + MODAL_BTN_GAP
        val rHov = mx in rX until rX + MODAL_BTN_W && my in bY until bY + MODAL_BTN_H
        Draw.roundedRect(ctx, rX, bY, MODAL_BTN_W, MODAL_BTN_H, 4, if (rHov) Draw.withAlpha(Theme.editorTheme.surface, 100) else Draw.withAlpha(Theme.editorTheme.mantle, 180))

        val rLbl = "[R] Reset"
        val rlw = tr.widthWithFont(rLbl)
        val rlx = rX + (MODAL_BTN_W - rlw) / 2
        val rly = bY + (MODAL_BTN_H - tr.lineHeight) / 2
        ctx.textWithFont(tr, rLbl, rlx, rly, Theme.textSecondary, false)
        Draw.rect(ctx, rlx - 7, rly + tr.lineHeight + 4, rlw + 14, 1, 0xFFF44336.toInt())
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (edFocus) lastInputTime = System.currentTimeMillis()

        val key = input.key()

        if (showUnsavedModal) {
            when (key) {
                GLFW.GLFW_KEY_S -> { saveAndClose(); return true }
                GLFW.GLFW_KEY_R -> { resetAndClose(); return true }
                GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_ESCAPE -> { cancelModal(); return true }
            }

            return true
        }

        val ctrl = input.modifiers() and GLFW.GLFW_MOD_CONTROL != 0
        val shift = input.modifiers() and GLFW.GLFW_MOD_SHIFT != 0
        val alt = input.modifiers() and GLFW.GLFW_MOD_ALT != 0

        if (ctrl && key == GLFW.GLFW_KEY_S) {
            save()
            return true
        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (dirty) {
                showUnsavedModal = true
                return true
            }

            onClose()

            return true
        }

        if (tree.isNotEmpty()) {
            when {
                alt && shift && key == GLFW.GLFW_KEY_LEFT -> {
                    treeWidth = (treeWidth - 5).coerceAtLeast(MIN_TREE_W)
                    return true
                }

                alt && shift && key == GLFW.GLFW_KEY_RIGHT -> {
                    treeWidth = (treeWidth + 5).coerceAtMost(MAX_TREE_W)
                    return true
                }

                alt && shift && (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) -> {
                    explorerVisible = !explorerVisible
                    return true
                }

                alt && key == GLFW.GLFW_KEY_UP -> {
                    treeFocusIdx = (treeFocusIdx - 1).coerceAtLeast(0)
                    treeScrollToFocus()
                    return true
                }

                alt && key == GLFW.GLFW_KEY_DOWN -> {
                    treeFocusIdx = (treeFocusIdx + 1).coerceAtMost(tree.size - 1)
                    treeScrollToFocus()
                    return true
                }

                alt && (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)
                        || ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && openFile == null) -> {
                    tree.getOrNull(treeFocusIdx)?.let { e ->
                        if (e.file.isDirectory) { expanded.add(e.file); rebuild() }
                        else openFile(e.file)
                    }

                    return true
                }

                alt && key == GLFW.GLFW_KEY_LEFT || alt && key == GLFW.GLFW_KEY_BACKSPACE -> {
                    tree.getOrNull(treeFocusIdx)?.let { e ->
                        if (e.file.isDirectory && e.file in expanded) {
                            expanded.remove(e.file); rebuild()
                        } else {
                            val parent = e.file.parentFile
                            if (parent != null && parent != ROOT && parent in expanded) {
                                expanded.remove(parent); rebuild()
                                treeFocusIdx = tree.indexOfFirst { it.file == parent }.coerceAtLeast(0)
                            }
                        }
                    }

                    return true
                }
            }
        }

        if (!edFocus) return super.keyPressed(input)

        if (ctrl) {
            if (key == undoKey) {
                if (shift) redo() else undo()
                clamp(); scroll()

                return true
            }

            if (key == redoKey && !shift) {
                redo()
                clamp()
                scroll()

                return true
            }

            when (key) {
                GLFW.GLFW_KEY_A -> {
                    selAnchor = 0 to 0; curLine = lines.size - 1; curCol = lines.last().length
                    return true
                }

                GLFW.GLFW_KEY_C -> {
                    setClip(if (hasSel()) selText() else lines[curLine])
                    return true
                }

                GLFW.GLFW_KEY_X -> {
                    pushUndo()

                    if (hasSel()) {
                        setClip(selText())
                        delSelNoUndo()
                    } else {
                        setClip(lines[curLine] + "\n")
                        if (lines.size > 1) lines.removeAt(curLine) else lines[curLine] = ""
                        curLine = curLine.coerceAtMost(lines.size - 1)
                        curCol = 0
                    }

                    dirty = true
                    clamp()
                    scroll()

                    return true
                }

                GLFW.GLFW_KEY_V -> {
                    val clip = getClip()

                    if (clip.isNotEmpty()) {
                        pushUndo()
                        if (hasSel()) delSelNoUndo()

                        val parts = clip.split("\n")
                        if (parts.size == 1) {
                            val ln = lines[curLine]

                            lines[curLine] = ln.take(curCol) + clip + ln.drop(curCol)
                            curCol += clip.length
                        } else {
                            val before = lines[curLine].take(curCol); val after = lines[curLine].drop(curCol)

                            lines[curLine] = before + parts.first()
                            parts.drop(1).dropLast(1).forEachIndexed { idx, p -> lines.add(curLine + 1 + idx, p) }
                            lines.add(curLine + parts.size - 1, parts.last() + after)

                            curLine += parts.size - 1; curCol = parts.last().length
                        }

                        dirty = true
                    }

                    clamp(); scroll(); return true
                }

                GLFW.GLFW_KEY_D -> {
                    pushUndo()

                    lines.add(curLine + 1, lines[curLine])
                    curLine++
                    dirty = true
                    scroll()

                    return true
                }

                GLFW.GLFW_KEY_K -> if (shift) {
                    pushUndo()

                    selAnchor = null
                    if (lines.size > 1) lines.removeAt(curLine) else lines[curLine] = ""
                    curLine = curLine.coerceAtMost(lines.size - 1)
                    curCol = curCol.coerceAtMost(lines[curLine].length)
                    dirty = true
                    scroll()

                    return true
                }

                GLFW.GLFW_KEY_SLASH -> {
                    pushUndo()
                    selAnchor = null

                    val ln = lines[curLine]; val ind = ln.length - ln.trimStart().length; val tr2 = ln.trimStart()
                    lines[curLine] = when {
                        tr2.startsWith("// ") -> " ".repeat(ind) + tr2.removePrefix("// ")
                        tr2.startsWith("//") -> " ".repeat(ind) + tr2.removePrefix("//")
                        else -> " ".repeat(ind) + "// " + tr2
                    }

                    dirty = true

                    return true
                }

                GLFW.GLFW_KEY_LEFT -> {
                    if (shift) {
                        if (selAnchor == null) selAnchor = curLine to curCol
                    } else {
                        selAnchor = null
                        moveWordLeft()
                        clamp()
                        scroll()
                        return true
                    }
                }

                GLFW.GLFW_KEY_RIGHT -> {
                    if (shift) {
                        if (selAnchor == null) selAnchor = curLine to curCol
                    } else {
                        selAnchor = null
                        moveWordRight()
                        clamp()
                        scroll()
                        return true
                    }
                }

                GLFW.GLFW_KEY_BACKSPACE -> {
                    pushUndo()
                    selAnchor = null

                    if (curCol > 0) {
                        val ln = lines[curLine]
                        val sc = curCol
                        moveWordLeft()
                        lines[curLine] = ln.removeRange(curCol, sc)
                    } else if (curLine > 0) {
                        val cur = lines.removeAt(curLine)
                        curLine--
                        curCol = lines[curLine].length
                        lines[curLine] += cur
                    }

                    dirty = true; clamp(); scroll(); return true
                }

                GLFW.GLFW_KEY_DELETE -> {
                    pushUndo()
                    selAnchor = null

                    val prevLine = curLine
                    val ln = lines[curLine]
                    val sc = curCol
                    moveWordRight()

                    if (curLine == prevLine && curCol > sc) {
                        lines[curLine] = ln.removeRange(sc, curCol)
                        curCol = sc
                    } else if (curLine > prevLine) {
                        lines[curLine - 1] += lines.removeAt(curLine)
                        curLine--
                        curCol = sc
                    }

                    dirty = true; clamp(); scroll(); return true
                }
            }
        }

        when (key) {
            GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                pushUndo()
                if (hasSel()) delSelNoUndo()

                val ln = lines[curLine]
                val indent = ln.takeWhile { it == ' ' || it == '\t' }
                lines[curLine] = ln.take(curCol)
                lines.add(curLine + 1, indent + ln.drop(curCol))
                curLine++
                curCol = indent.length
                dirty = true
            }

            GLFW.GLFW_KEY_BACKSPACE -> {
                pushUndo()

                when {
                    hasSel() -> delSelNoUndo()

                    curCol > 0 -> {
                        lines[curLine] = lines[curLine].removeRange(curCol - 1, curCol)
                        curCol--
                    }

                    curLine > 0 -> {
                        val cur = lines.removeAt(curLine)
                        curLine--
                        curCol = lines[curLine].length
                        lines[curLine] += cur
                    }
                }

                dirty = true
            }

            GLFW.GLFW_KEY_DELETE -> {
                pushUndo()

                when {
                    hasSel() -> delSelNoUndo()
                    curCol < lines[curLine].length -> lines[curLine] = lines[curLine].removeRange(curCol, curCol + 1)
                    curLine < lines.size - 1 -> lines[curLine] += lines.removeAt(curLine + 1)
                }

                dirty = true
            }

            GLFW.GLFW_KEY_LEFT -> {
                if (shift) {
                    if (selAnchor == null) selAnchor = curLine to curCol
                } else if (hasSel()) {
                    val (s, _) = selRange()
                    curLine = s.first; curCol = s.second
                    selAnchor = null; clamp(); scroll()

                    return true
                } else {
                    selAnchor = null
                }

                if (curCol > 0) {
                    curCol--
                } else if (curLine > 0) {
                    curLine--
                    curCol = lines[curLine].length
                }
            }

            GLFW.GLFW_KEY_RIGHT -> {
                if (shift) {
                    if (selAnchor == null) selAnchor = curLine to curCol
                } else if (hasSel()) {
                    val (_, e) = selRange()
                    curLine = e.first; curCol = e.second
                    selAnchor = null; clamp(); scroll()
                    return true
                } else {
                    selAnchor = null
                }

                if (curCol < lines[curLine].length) curCol++
                else if (curLine < lines.size - 1) { curLine++; curCol = 0 }
            }

            GLFW.GLFW_KEY_UP -> {
                if (shift) {
                    if (selAnchor == null) selAnchor = curLine to curCol
                } else selAnchor = null

                if (curLine > 0) {
                    curLine--
                    curCol = curCol.coerceAtMost(lines[curLine].length)
                } else curCol = 0
            }

            GLFW.GLFW_KEY_DOWN -> {
                if (shift) {
                    if (selAnchor == null) selAnchor = curLine to curCol
                } else selAnchor = null

                if (curLine < lines.size - 1) {
                    curLine++
                    curCol = curCol.coerceAtMost(lines[curLine].length)
                } else curCol = lines[curLine].length
            }

            GLFW.GLFW_KEY_HOME -> {
                if (shift) {
                    if (selAnchor == null) selAnchor = curLine to curCol
                } else selAnchor = null

                curCol = 0
            }

            GLFW.GLFW_KEY_END -> {
                if (shift) {
                    if (selAnchor == null) selAnchor = curLine to curCol
                } else selAnchor = null

                curCol = lines[curLine].length
            }

            GLFW.GLFW_KEY_PAGE_UP -> {
                if (shift) {
                    if (selAnchor == null) selAnchor = curLine to curCol
                } else selAnchor = null

                curLine = (curLine - 10).coerceAtLeast(0)
                curCol = curCol.coerceAtMost(lines[curLine].length)
            }

            GLFW.GLFW_KEY_PAGE_DOWN -> {
                if (shift) {
                    if (selAnchor == null) selAnchor = curLine to curCol
                } else selAnchor = null

                curLine = (curLine + 10).coerceAtMost(lines.size - 1)
                curCol = curCol.coerceAtMost(lines[curLine].length)
            }

            GLFW.GLFW_KEY_TAB -> {
                if (shift) {
                    pushUndo()

                    val ln = lines[curLine]
                    when {
                        ln.startsWith("    ") -> {
                            lines[curLine] = ln.drop(4)
                            curCol = (curCol - 4).coerceAtLeast(0)
                            dirty = true
                        }

                        ln.startsWith("\t") -> {
                            lines[curLine] = ln.drop(1)
                            curCol = (curCol - 1).coerceAtLeast(0)
                            dirty = true
                        }
                    }
                } else {
                    insert("    ")
                }
            }

            else -> return super.keyPressed(input)
        }

        clamp()
        scroll()

        return true
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        if (!edFocus || !input.isAllowedChatCharacter()) return false
        insert(input.codepointAsString())

        return true
    }

    private fun clamp() { curLine = curLine.coerceIn(0, lines.size - 1); curCol = curCol.coerceIn(0, lines[curLine].length) }

    private fun scroll() {
        val vis = (height - HDR_H - STATUS_H) / (font.lineHeight + 3)
        if (curLine < edScroll) edScroll = curLine
        if (curLine >= edScroll + vis) edScroll = curLine - vis + 1
        edScroll = edScroll.coerceAtLeast(0)

        if (openFile != null && lines.isNotEmpty() && curLine < lines.size) {
            val gutterW  = font.widthWithFont("0".repeat(lines.size.toString().length)) + 8
            val codeAreaW = ((width - SB_PADDING - SB_THICKNESS) - (etw + 1 + gutterW + 1)).coerceAtLeast(1)
            val curRelX   = colToVisualX(font, lines[curLine], curCol, 0)

            if (curRelX < edHScroll) edHScroll = curRelX
            if (curRelX > edHScroll + codeAreaW - 4) edHScroll = curRelX - codeAreaW + 4

            val maxW = lines.maxOfOrNull { font.widthWithFont(it) + 4 } ?: 0
            edHScroll = edHScroll.coerceIn(0, (maxW - codeAreaW).coerceAtLeast(0))
        }
    }

    private fun treeScrollToFocus() {
        val lh = font.lineHeight + 3; val aH = height - HDR_H - STATUS_H
        val ey = treeFocusIdx * lh
        if (ey < treeScroll) treeScroll = ey
        if (ey + lh > treeScroll + aH) treeScroll = (ey + lh - aH).coerceAtLeast(0)
    }

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        val mx = click.x().toInt(); val my = click.y().toInt()
        val lh = font.lineHeight + 3

        if (showUnsavedModal) {
            val modalX = (width  - MODAL_W) / 2
            val modalY = (height - MODAL_H) / 2
            val totalBtnW  = MODAL_BTN_W * 3 + MODAL_BTN_GAP * 2
            val bX = modalX + (MODAL_W - totalBtnW) / 2
            val bY = modalY + MODAL_H - MODAL_BTN_H - 12
            val sX = bX
            val cX = bX + MODAL_BTN_W + MODAL_BTN_GAP
            val rX = cX + MODAL_BTN_W + MODAL_BTN_GAP

            when {
                mx in sX until sX + MODAL_BTN_W && my in bY until bY + MODAL_BTN_H -> saveAndClose()
                mx in cX until cX + MODAL_BTN_W && my in bY until bY + MODAL_BTN_H -> cancelModal()
                mx in rX until rX + MODAL_BTN_W && my in bY until bY + MODAL_BTN_H -> resetAndClose()

                mx < modalX || mx >= modalX + MODAL_W ||
                my < modalY || my >= modalY + MODAL_H -> cancelModal()
            }

            return true
        }

        if (openFile != null && click.button() == 0) {
            val cy1 = HDR_H; val cy2 = height - STATUS_H
            val lh = font.lineHeight + 3
            val vBarX = width - SB_PADDING - SB_THICKNESS
            val vBarHitX = vBarX - 1
            if (mx >= vBarHitX && my in cy1 + SB_PADDING until cy2 - SB_PADDING) {
                val trackH = cy2 - cy1 - 2 * SB_PADDING
                val total = lines.size.coerceAtLeast(1)
                val visible = ((cy2 - cy1) / lh).coerceAtLeast(1)
                val maxScr = (total - visible).coerceAtLeast(0)

                edScroll = ((my - cy1 - SB_PADDING).toFloat() / trackH * total - visible / 2).toInt().coerceIn(0, maxScr)
                draggingVBar = true; dragStartY = my; dragStartVScroll = edScroll

                return true
            }
            val hBarY  = cy2 - SB_PADDING - SB_THICKNESS
            val hBarX0 = etw + 1 + SB_PADDING
            val hBarX1 = width - SB_PADDING - SB_THICKNESS - SB_PADDING
            if (my in hBarY - 1..hBarY + SB_THICKNESS && mx in hBarX0 until hBarX1) {
                val gutterW  = font.widthWithFont("0".repeat(lines.size.toString().length)) + 8
                val codeAreaW = ((width - SB_PADDING - SB_THICKNESS) - (etw + 1 + gutterW + 1)).coerceAtLeast(1)
                val trackW = hBarX1 - hBarX0
                val maxW = lines.maxOfOrNull { font.widthWithFont(it) + 4 } ?: 0
                val maxHScr = (maxW - codeAreaW).coerceAtLeast(0)

                edHScroll = ((mx - hBarX0).toFloat() / trackW * maxW - codeAreaW / 2).toInt().coerceIn(0, maxHScr)
                draggingHBar = true; dragStartX = mx; dragStartHScroll = edHScroll

                return true
            }
        }

        if (mx < etw && my >= HDR_H) {
            edFocus = false; selAnchor = null

            val idx = ((my - HDR_H + treeScroll) / lh - 1).coerceAtLeast(0)
            if (idx in tree.indices) {
                treeFocusIdx = idx

                val e = tree[idx]
                if (e.file.isDirectory) {
                    if (e.file in expanded) expanded.remove(e.file) else expanded.add(e.file)
                    rebuild()
                } else if (e.file.isFile) openFile(e.file)
            }

            return true
        }

        if (mx > etw && my >= HDR_H && my < height - STATUS_H && openFile != null) {
            edFocus = true
            val gutterW = font.widthWithFont("0".repeat(lines.size.toString().length)) + 8
            val codeX = etw + 1 + gutterW + 2 - edHScroll

            val clickedLine = (edScroll + (my - HDR_H) / lh).coerceIn(0, lines.size - 1)
            curLine = clickedLine
            val raw = lines[curLine]

            curCol = visualXToCol(font, raw, mx, codeX).coerceAtMost(raw.length)

            if (doubled) {
                val ln = lines[curLine]; var ws = curCol; var we = curCol
                while (ws > 0 && ln[ws - 1].isWord()) ws--
                while (we < ln.length && ln[we].isWord()) we++
                if (we > ws) { selAnchor = curLine to ws; curCol = we } else selAnchor = null
            } else {
                selAnchor = curLine to curCol
                dragging = true
            }

            return true
        }

        return super.mouseClicked(click, doubled)
    }

    override fun mouseDragged(click: MouseButtonEvent, offsetX: Double, offsetY: Double): Boolean {
        if (draggingVBar) {
            val my = (click.y() + offsetY).toInt()
            val cy1 = HDR_H; val cy2 = height - STATUS_H
            val lh = font.lineHeight + 3
            val trackH = cy2 - cy1 - 2 * SB_PADDING
            val total = lines.size.coerceAtLeast(1)
            val visible = ((cy2 - cy1) / lh).coerceAtLeast(1)
            val maxScr = (total - visible).coerceAtLeast(1)
            val thumbH = (trackH * visible.toFloat() / total).toInt().coerceAtLeast(8).coerceAtMost(trackH)
            val scrollable = (trackH - thumbH).coerceAtLeast(1)

            edScroll = (dragStartVScroll + ((my - dragStartY).toFloat() / scrollable * maxScr).toInt()).coerceIn(0, maxScr)
            return true
        }

        if (draggingHBar) {
            val mx = (click.x() + offsetX).toInt()
            val gutterW = font.widthWithFont("0".repeat(lines.size.toString().length)) + 8
            val codeAreaW = ((width - SB_PADDING - SB_THICKNESS) - (etw + 1 + gutterW + 1)).coerceAtLeast(1)
            val trackX = etw + 1 + SB_PADDING
            val trackW = (width - SB_PADDING - SB_THICKNESS) - SB_PADDING - trackX
            val maxW = lines.maxOfOrNull { font.widthWithFont(it) + 4 } ?: 0
            val maxHScr = (maxW - codeAreaW).coerceAtLeast(1)
            val thumbW = (trackW * codeAreaW.toFloat() / maxW).toInt().coerceAtLeast(8).coerceAtMost(trackW)
            val scrollable = (trackW - thumbW).coerceAtLeast(1)

            edHScroll = (dragStartHScroll + ((mx - dragStartX).toFloat() / scrollable * maxHScr).toInt()).coerceIn(0, maxHScr)
            return true
        }

        if (dragging && edFocus && openFile != null) {
            val mx = (click.x() + offsetX).toInt(); val my = (click.y() + offsetY).toInt()
            val lh = font.lineHeight + 3
            val gutterW = font.widthWithFont("0".repeat(lines.size.toString().length)) + 8
            val codeX = etw + 1 + gutterW + 2 - edHScroll

            val clickedLine = (edScroll + (my - HDR_H) / lh).coerceIn(0, lines.size - 1)
            curLine = clickedLine
            val raw = lines[curLine]
            curCol = visualXToCol(font, raw, mx, codeX).coerceAtMost(raw.length)

            clamp()
            scroll()

            return true
        }

        return super.mouseDragged(click, offsetX, offsetY)
    }

    override fun mouseReleased(click: MouseButtonEvent): Boolean {
        if (draggingVBar || draggingHBar) {
            draggingVBar = false; draggingHBar = false
            return true
        }

        if (dragging) {
            dragging = false

            if (selAnchor != null && selAnchor!!.first == curLine && selAnchor!!.second == curCol) {
                selAnchor = null
            }

            return true
        }

        return super.mouseReleased(click)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, hAmt: Double, vAmt: Double): Boolean {
        val lh = font.lineHeight + 3
        if (mouseX < etw) {
            treeScroll = (treeScroll - (vAmt * lh * 3).toInt()).coerceAtLeast(0)
        } else {
            if (vAmt != 0.0)
                edScroll = (edScroll - (vAmt * 3).toInt()).coerceIn(0, (lines.size - 1).coerceAtLeast(0))
            if (hAmt != 0.0 && openFile != null) {
                val gutterW = font.widthWithFont("0".repeat(lines.size.toString().length)) + 8
                val codeAreaW = ((width - SB_PADDING - SB_THICKNESS) - (etw + 1 + gutterW + 1)).coerceAtLeast(1)
                val maxW = lines.maxOfOrNull { font.widthWithFont(it) + 4 } ?: 0

                edHScroll = (edHScroll + (hAmt * 20).toInt()).coerceIn(0, (maxW - codeAreaW).coerceAtLeast(0))
            }
        }
        return true
    }

    override fun onClose() {
        saveState()
        super.onClose()
    }
}
