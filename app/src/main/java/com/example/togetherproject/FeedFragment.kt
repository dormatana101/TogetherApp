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
    /*itemView.setOnClickListener{
            adapterPosition
        }
        itemView.findViewById<View>(R.id.student_row).setOnClickListener {
            val intent = Intent(itemView.context, StudentDetailsActivity::class.java).apply {
                putExtra("STUDENT_ID", student?.id)
                putExtra("STUDENT_NAME", student?.name)
                putExtra("STUDENT_ADDRESS", student?.address)
                putExtra("STUDENT_PHONE", student?.phone)
                putExtra("STUDENT_IS_CHECKED", student?.isChecked)
                putExtra("STUDENT_INDEX", adapterPosition)
            }
            itemView.context.startActivity(intent)
        }
*/


    fun bind(post: Post) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        profileNameTextView?.text = post.profileName
        postTextView?.text = post.textPost
        dateTextView?.text = dateFormat.format(post.date)

       // Picasso.get().load(uri).into(profileImage)
        if(post.ProfileImage != "image") {
            try {
                Picasso.get()
                    .load(post.ProfileImage)
                    .transform(CropCircleTransformation())
                    .into(imageProfile)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the error, e.g., set a placeholder image
                //imagePost!!.setImageResource(R.drawable.image)
            }
        }

        if (imagePost != null) {
            try {
                imagePost?.visibility = View.VISIBLE
                Picasso.get().load(post.imagePost).into(imagePost)
            } catch (e: Exception) {
                imagePost?.visibility = View.GONE
                e.printStackTrace()
                // Handle the error, e.g., set a placeholder image
                //imagePost!!.setImageResource(R.drawable.image)
            }
        }
        //val resourceId = itemView.context.resources.getIdentifier(post.imagePost, "drawable", itemView.context.packageName)
        //imagePost?.setImageResource(resourceId)



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
    //var adapter: PostRecycleAdapter? = null
    var adapter = PostRecycleAdapter(Model.instance.postList)
    var posts: MutableList<Post> = ArrayList()
    private lateinit var progressBar: ProgressBar
    private lateinit var  recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_feed, container, false)

        progressBar = view.findViewById(R.id.feedProgressBar)

        posts = Model.instance.postList

        recyclerView = view.findViewById(R.id.fragment_feed_recycler_view)
        recyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        adapter = PostRecycleAdapter(posts)
        recyclerView.adapter = adapter
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

        Model.instance.retrievePosts { fetchedPosts ->

            posts.clear()
            posts.addAll(fetchedPosts)

            adapter?.set(posts)
            adapter?.notifyDataSetChanged()
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}