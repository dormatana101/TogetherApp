package com.example.togetherproject

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.togetherproject.model.Model
import com.example.togetherproject.model.Post
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log
import java.util.TimeZone

class MyPostsViewHolder(itemView: View, private val onEditClick: (String) -> Unit) :
    RecyclerView.ViewHolder(itemView) {
    var profileNameTextView: TextView? = null
    var postTextView: TextView? = null
    var imageProfile: ImageView? = null
    var dateTextView: TextView? = null
    var imagePost: ImageView? = null
    var editButton: ImageView? = null
    var deleteButton: ImageView? = null



    init {
        profileNameTextView = itemView.findViewById(R.id.profileName)
        postTextView = itemView.findViewById(R.id.textPost)
        dateTextView = itemView.findViewById(R.id.postDate)
        imagePost = itemView.findViewById(R.id.imagePost)
        imageProfile = itemView.findViewById(R.id.ProfileImage)
        editButton = itemView.findViewById(R.id.editPostIcon)
        deleteButton = itemView.findViewById(R.id.deletePostIcon)
    }

    fun bind(post: Post) {
        // Set the date format and time zone
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jerusalem")  // Change this to your local time zone

        // Bind data to views
        profileNameTextView?.text = post.name
        postTextView?.text = post.content
        dateTextView?.text = dateFormat.format(post.timestamp)  // Format timestamp and set to TextView

        // Set profile image
        if (post.profileImage != "image") {
            try {
                Picasso.get()
                    .load(post.profileImage)
                    .transform(CropCircleTransformation())
                    .into(imageProfile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Set post image
        if (imagePost != null) {
            try {
                imagePost?.visibility = View.VISIBLE
                Picasso.get().load(post.imageUrl).into(imagePost)
            } catch (e: Exception) {
                imagePost?.visibility = View.GONE
                e.printStackTrace()
            }
        }

        // Set up edit button click listener
        editButton?.setOnClickListener {
            onEditClick(post.id)
        }

        // Set up delete button click listener
        deleteButton?.setOnClickListener {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes") { dialog, _ ->
                    Model.instance.deletePost(post.id) { success, _ ->
                        if (success) {
                            Toast.makeText(itemView.context, "Your post was deleted successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(itemView.context, "Connection failed", Toast.LENGTH_LONG).show()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

}

class MyPostRecycleAdapter(
    private var posts: List<Post>?,
    private val onEditClick: (String) -> Unit
) : RecyclerView.Adapter<MyPostsViewHolder>() {
    override fun getItemCount(): Int = posts?.size ?: 0

    fun set(_posts: List<Post>) {
        posts = _posts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_post_row, parent, false)
        return MyPostsViewHolder(view, onEditClick)
    }

    override fun onBindViewHolder(holder: MyPostsViewHolder, position: Int) {
        holder.bind(posts?.get(position) ?: return)
    }
}

class MyPostsFragment : Fragment() {
    private var adapter: MyPostRecycleAdapter? = null
    private var posts: MutableList<Post> = ArrayList()
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_posts, container, false)


        progressBar = view.findViewById(R.id.feedProgressBar)
        recyclerView = view.findViewById(R.id.fragment_feed_recycler_view)
        emptyView = view.findViewById(R.id.emptyView)


        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = MyPostRecycleAdapter(posts) { postId ->
            editPostButtonClicked(postId)
        }
        recyclerView.adapter = adapter

        getMyPosts()

        val fabCreatePost = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_create_post)
        fabCreatePost.setOnClickListener {
            (activity as? MainActivity)?.handleAddPostClick(false, null)
        }


        return view
    }

    override fun onResume() {
        super.onResume()
        getMyPosts()
    }

    private fun getMyPosts() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        Model.instance.retrieveUserPosts { fetchedPosts ->
            progressBar.visibility = View.GONE
            posts.clear()
            posts.addAll(fetchedPosts)
            adapter?.set(posts)
            adapter?.notifyDataSetChanged()

            if (posts.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }


    private fun editPostButtonClicked(postId: String?) {
        (activity as? MainActivity)?.handleAddPostClick(true, postId)
    }

}
