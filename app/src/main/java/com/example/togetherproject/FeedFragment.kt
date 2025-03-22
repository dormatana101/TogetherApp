package com.example.togetherproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.togetherproject.model.Model
import com.example.togetherproject.model.Post
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.text.SimpleDateFormat
import java.util.Locale

class PostsViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    var profileNameTextView: TextView? = null
    var postTextView: TextView? = null
    var imageProfile: ImageView? = null
    var dateTextView: TextView? = null
    var imagePost: ImageView? = null




    init {
        profileNameTextView = itemView.findViewById(R.id.profileName)
        postTextView = itemView.findViewById(R.id.textPost)
        dateTextView = itemView.findViewById(R.id.postDate)
        imagePost = itemView.findViewById(R.id.imagePost)
        imageProfile = itemView.findViewById(R.id.ProfileImage)

    }


    fun bind(post: Post) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        profileNameTextView?.text = post.name
        postTextView?.text = post.content
        dateTextView?.text = dateFormat.format(post.timestamp)

        if(post.profileImage != "image") {
            try {
                Picasso.get()
                    .load(post.profileImage)
                    .transform(CropCircleTransformation())
                    .into(imageProfile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (imagePost != null) {
            try {
                imagePost?.visibility = View.VISIBLE
                Picasso.get().load(post.imageUrl).into(imagePost)
            } catch (e: Exception) {
                imagePost?.visibility = View.GONE
                e.printStackTrace()
            }
        }




    }

}

class PostRecycleAdapter(private var posts : List<Post>?): RecyclerView.Adapter<PostsViewHolder>() {
    override fun getItemCount(): Int {
        return posts?.size ?: 0
    }
    fun set(_posts: List<Post>) {
        posts = _posts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val inflation=LayoutInflater.from(parent.context)
        val view = inflation.inflate(R.layout.post_row, parent, false)
        return PostsViewHolder(view);
    }


    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        holder.bind(posts?.get(position) ?: return)
    }


}


class FeedFragment : Fragment() {
    var adapter = PostRecycleAdapter(Model.instance.postList)
    var posts: MutableList<Post> = ArrayList()
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

        posts =  mutableListOf()
        emptyView = view.findViewById(R.id.emptyView)

        recyclerView = view.findViewById(R.id.fragment_feed_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = PostRecycleAdapter(posts)
        recyclerView.adapter = adapter

        val fabCreatePost = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_create_post)
        fabCreatePost.setOnClickListener {
            (activity as? MainActivity)?.handleAddPostClick(false, null)
        }
        getAllPosts()
        return view
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        getAllPosts()

    }

    private fun getAllPosts() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        Model.instance.retrievePosts { fetchedPosts ->
            posts.clear()
            posts.addAll(fetchedPosts)

            adapter?.set(posts)
            adapter?.notifyDataSetChanged()

            progressBar.visibility = View.GONE

            if (posts.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }
}