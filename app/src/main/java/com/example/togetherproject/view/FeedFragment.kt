package com.example.togetherproject.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.togetherproject.model.Post
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.ViewModelProvider
import com.example.togetherproject.R
import com.example.togetherproject.viewmodel.FeedViewModel



class PostsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val profileNameTextView: TextView = itemView.findViewById(R.id.profileName)
    private val postTextView: TextView = itemView.findViewById(R.id.textPost)
    private val imageProfile: ImageView = itemView.findViewById(R.id.ProfileImage)
    private val dateTextView: TextView = itemView.findViewById(R.id.postDate)
    private val imagePost: ImageView = itemView.findViewById(R.id.imagePost)

    fun bind(post: Post) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        profileNameTextView.text = post.name
        postTextView.text = post.content
        dateTextView.text = dateFormat.format(post.timestamp)

        if (post.profileImage == "image" || post.profileImage == "ic_user") {
            imageProfile.setImageResource(R.drawable.ic_user)
        } else {
            try {
                Picasso.get()
                    .load(post.profileImage)
                    .transform(CropCircleTransformation())
                    .into(imageProfile)
            } catch (e: Exception) {
                e.printStackTrace()
                imageProfile.setImageResource(R.drawable.ic_user)
            }
        }

        try {
            imagePost.visibility = View.VISIBLE
            Picasso.get().load(post.imageUrl).into(imagePost)
        } catch (e: Exception) {
            imagePost.visibility = View.GONE
            e.printStackTrace()
        }
    }
}


class PostRecycleAdapter(private var posts: List<Post>?) : RecyclerView.Adapter<PostsViewHolder>() {
    override fun getItemCount(): Int {
        return posts?.size ?: 0
    }

    fun set(_posts: List<Post>) {
        posts = _posts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val inflation = LayoutInflater.from(parent.context)
        val view = inflation.inflate(R.layout.post_row, parent, false)
        return PostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        holder.bind(posts?.get(position) ?: return)
    }
}

class FeedFragment : Fragment() {
    private lateinit var adapter: PostRecycleAdapter
    private var posts: MutableList<Post> = mutableListOf()
    private lateinit var viewModel: FeedViewModel
    private lateinit var emptyView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feed, container, false)

        progressBar = view.findViewById(R.id.feedProgressBar)
        emptyView = view.findViewById(R.id.emptyView)

        recyclerView = view.findViewById(R.id.fragment_feed_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PostRecycleAdapter(posts)
        recyclerView.adapter = adapter

        // 🟩 יצירת ViewModel
        viewModel = ViewModelProvider(this)[FeedViewModel::class.java]

        // 🟩 תצפית על הפוסטים
        viewModel.postsLiveData.observe(viewLifecycleOwner) { fetchedPosts ->
            posts.clear()
            posts.addAll(fetchedPosts)
            adapter.set(posts)
            adapter.notifyDataSetChanged()

            if (posts.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        // 🟩 תצפית על מצב טעינה
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.loadPosts()

        return view
    }

}
