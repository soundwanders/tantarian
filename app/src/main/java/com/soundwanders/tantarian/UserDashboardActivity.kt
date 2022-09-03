package com.soundwanders.tantarian

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.databinding.ActivityUserDashboardBinding

class UserDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        // sign out on click
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )
        categoryArrayList = ArrayList()

        // load categories from Firebase
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = ModelCategory("01", "All", 1, "")
                val modelMostViewed = ModelCategory("01", "Most Viewed", 1, "")
                val modelMostDownloaded= ModelCategory("01", "Most Downloaded", 1, "")

                // add to viewPagerAdapter
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)

                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}",
                    ), modelAll.category
                )

                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}",
                    ), modelMostViewed.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",
                        "${modelMostDownloaded.uid}",
                    ), modelMostDownloaded.category
                )
                // refresh fragment list
                viewPagerAdapter.notifyDataSetChanged()

                // load data from Firebase
                for (ds in snapshot.children) {
                    // get data from model
                    val model = ds.getValue(ModelCategory::class.java)

                    // add to your Category array list
                    categoryArrayList.add(model!!)

                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}",
                        ), model.category
                    )

                    // refresh list
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) : FragmentPagerAdapter(fm, behavior) {
        // acts as a container to hold array list of fragments
        private val fragmentList: ArrayList<BooksUserFragment> = ArrayList()

        // list of the titles of Categories
        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context: Context

        init {
            this.context = context
        }

        override fun getCount() : Int{
            return fragmentList.size
        }
        override fun getItem(position: Int) : Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int) : CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: BooksUserFragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)

        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        // if user null, return to Main screen
        if (firebaseUser == null) {
            binding.subTitleTv.text = getString(R.string.user_not_logged_in)
        }
        else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email
        }
    }
}