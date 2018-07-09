package example.daniel.fuckingandroid;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);

        firebaseAuth = FirebaseAuth.getInstance();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachButton = !recyclerView.canScrollVertically(-1);

                    if (reachButton)
                    {

                        String desc = lastVisible.getString("desc");
                        //Toast.makeText(container.getContext(), "Reached :" + desc, Toast.LENGTH_SHORT).show();

                        loadMorePost();
                    }

                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);

            //firebaseFirestore.collection("Posts").addSnapShopListener......
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() -1);

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                            blog_list.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();
                        }

                    }

                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost()
    {
        Query nextQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible).limit(3);

        //firebaseFirestore.collection("Posts").addSnapShopListener......
        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {

                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                            blog_list.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();
                        }

                    }

                }
                else
                {
                    //Toast.makeText(getContext(),"There are no Post",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
