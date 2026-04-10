package dev.losterixx.ttyclient.client.config

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

class CommentedJsonWriter(private val gson: Gson) {

    fun write(obj: Any): String = buildString {
        val tree = gson.toJsonTree(obj)

        if (tree is JsonObject) {
            writeObject(obj.javaClass, tree, this, "", emptySet(), "")
        } else {
            append(tree.toString())
        }

        append("\n")
    }

    fun writeTree(type: Class<*>, tree: JsonObject, outdatedKeys: Set<String> = emptySet()): String = buildString {
        writeObject(type, tree, this, "", outdatedKeys, "")
        append("\n")
    }

    private fun writeObject(
        type: Class<*>?,
        obj: JsonObject,
        out: StringBuilder,
        indent: String,
        outdatedKeys: Set<String>,
        currentPath: String
    ) {
        val inner = "$indent    "

        val meta = LinkedHashMap<String, FieldMeta>()
        type?.declaredFields?.forEach { f ->
            f.isAccessible = true
            val key = f.getAnnotation(SerializedName::class.java)?.value ?: f.name

            meta[key] = FieldMeta(
                comment = f.getAnnotation(Comment::class.java),
                type = f.type.takeUnless { it.isPrimitive || it == String::class.java }
            )
        }

        out.append("{\n")
        val entries = obj.entrySet().toList()
        entries.forEachIndexed { idx, (key, value) ->
            val (comment, childType) = meta[key] ?: FieldMeta(null, null)
            val fullPath = if (currentPath.isEmpty()) key else "$currentPath.$key"
            val isOutdated = fullPath in outdatedKeys

            if (comment != null && idx > 0) out.append("\n")

            comment?.lines?.forEach { line -> out.append("$inner// $line\n") }

            out.append("$inner${gson.toJson(key)}: ")
            writeValue(if (value is JsonObject) childType else null, value, out, inner, outdatedKeys, fullPath)

            if (idx < entries.size - 1) out.append(",")
            if (isOutdated) out.append("   // OUTDATED")
            out.append("\n")
        }
        out.append("$indent}")
    }

    private fun writeValue(
        type: Class<*>?,
        elem: JsonElement,
        out: StringBuilder,
        indent: String,
        outdatedKeys: Set<String>,
        currentPath: String
    ) {
        when {
            elem is JsonObject -> writeObject(type, elem, out, indent, outdatedKeys, currentPath)
            elem is JsonArray -> {
                val list = elem.toList()

                if (list.isEmpty()) {
                    out.append("[]")
                } else {
                    val inner = "$indent    "
                    out.append("[\n")
                    list.forEachIndexed { idx, child ->
                        out.append(inner)
                        writeValue(null, child, out, inner, outdatedKeys, currentPath)
                        if (idx < list.size - 1) out.append(",")
                        out.append("\n")
                    }
                    out.append("$indent]")
                }
            }
            else -> out.append(elem.toString())
        }
    }

    private data class FieldMeta(val comment: Comment?, val type: Class<*>?)
}
