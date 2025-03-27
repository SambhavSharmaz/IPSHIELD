package com.example.ipshield

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.text.format.DateFormat
import com.google.firebase.Timestamp
import android.widget.ArrayAdapter

class CommentAdapter(
    context: Context,
    private var comments: MutableList<Comment>
) : ArrayAdapter<Comment>(context, R.layout.comment_item, comments) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val comment = comments[position]
        holder.commentText.text = "${comment.username}: ${comment.text}"

        val formattedTime = try {
            DateFormat.format("hh:mm a, dd MMM yyyy", comment.timestamp.toDate())
        } catch (e: Exception) {
            "Unknown Time"
        }

        holder.timestampText.text = "⏳ $formattedTime"

        return view
    }

    private class ViewHolder(view: View) {
        val commentText: TextView = view.findViewById(R.id.comment_text)
        val timestampText: TextView = view.findViewById(R.id.comment_timestamp)
    }

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}

data class Comment(val text: String, val username: String, val timestamp: Timestamp)
