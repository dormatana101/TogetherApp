package com.example.togetherproject

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.togetherproject.model.Model
import com.example.togetherproject.model.Post
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.togetherproject.FeedFragment
import com.example.togetherproject.base.MyApplication.Globals.context

class MyPostsViewHolder (itemView: View, private val onEditClick: (String) -> Unit): RecyclerView.ViewHolder(itemView) {
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
        editButton=itemView.findViewById(R.id.editPostIcon)
        deleteButton=itemView.findViewById(R.id.deletePostIcon)

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

        editButton?.setOnClickListener(){
            onEditClick(post.id)
        }
        deleteButton?.setOnClickListener {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes") { dialog, _ ->
                    Model.instance.deletePost(post.id) { success, _ ->
                        if (success) {
                            Toast.makeText(context, "Your post was deleted successfully!", Toast.LENGTH_LONG).show()
                            (itemView.context as? MainActivity)?.handleMyPostsClick()
                        } else {
                            Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()
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

class MyPostRecycleAdapter(private var posts : List<Post>?, private val onEditClick: (String) -> Unit): RecyclerView.Adapter<MyPostsViewHolder>() {
    override fun getItemCount(): Int {
        return posts?.size ?: 0
    }
    fun set(_posts: List<Post>) {
        posts = _posts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostsViewHolder {
        val inflation=LayoutInflater.from(parent.context)
        val view = inflation.inflate(R.layout.my_post_row, parent, false)

        return MyPostsViewHolder(view, onEditClick);
    }


    override fun onBindViewHolder(holder: MyPostsViewHolder, position: Int) {
        holder.bind(posts?.get(position) ?: return)
    }


}




class MyPostsFragment : Fragment() {
    var adapter: MyPostRecycleAdapter? = null
    var posts: MutableList<Post> = ArrayList()
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_feed, container, false)
        progressBar = view.findViewById(R.id.feedProgressBar)
        recyclerView = view.findViewById(R.id.fragment_feed_recycler_view)

        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        posts = Model.instance.postList
        recyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        adapter = MyPostRecycleAdapter(posts){postId->
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
    fun editPostButtonClicked(postId: String?) {
        (activity as? MainActivity)?.editPost(postId)
    }

    private fun getMyPosts() {

        Model.instance.retrieveUserPosts { fetchedPosts ->
            posts.clear()
            posts.addAll(fetchedPosts)

            adapter?.set(posts)
            adapter?.notifyDataSetChanged()
        }
    }
}