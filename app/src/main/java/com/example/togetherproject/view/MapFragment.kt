package com.example.togetherproject.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.Fragment
import com.example.togetherproject.R
import com.example.togetherproject.data.Place
import com.example.togetherproject.data.PlaceRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import androidx.core.widget.addTextChangedListener


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheet: LinearLayout

    private lateinit var bottomSheetTitle: MaterialTextView
    private lateinit var bottomSheetAddress: MaterialTextView
    private lateinit var bottomSheetCity: MaterialTextView
    private lateinit var bottomSheetPhone: MaterialTextView
    private lateinit var bottomSheetWorkingHours: MaterialTextView
    private lateinit var bottomSheetDescription: MaterialTextView
    private lateinit var bottomSheetPhoto: ShapeableImageView

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchAdapter: PlaceSearchAdapter

    private lateinit var myLocationFab: FloatingActionButton

    private val places: List<Place> = PlaceRepository.places

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        searchAdapter = PlaceSearchAdapter(emptyList()) { selectedPlace ->
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(selectedPlace.lat, selectedPlace.lng), 14f
                )
            )
            searchResultsRecyclerView.visibility = View.GONE
        }
        searchResultsRecyclerView.adapter = searchAdapter
        searchResultsRecyclerView.visibility = View.GONE

        searchEditText.addTextChangedListener {editable ->
            val query = editable?.toString()?.trim() ?: ""
            if (query.isEmpty()) {
                searchResultsRecyclerView.visibility = View.GONE
                addMarkers(places)
            } else {
                val filtered = places.filter { place ->
                    place.name.contains(query, ignoreCase = true) ||
                            place.city.contains(query, ignoreCase = true)
                }
                if (filtered.isNotEmpty()) {
                    searchAdapter.setData(filtered)
                    searchResultsRecyclerView.visibility = View.VISIBLE
                } else {
                    searchResultsRecyclerView.visibility = View.GONE
                    Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show()
                }
            }
        }


        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isEmpty()) {
                searchResultsRecyclerView.visibility = View.GONE
            } else {
                val filtered = places.filter { place ->
                    place.name.contains(query, ignoreCase = true) ||
                            place.city.contains(query, ignoreCase = true)
                }
                if (filtered.isNotEmpty()) {
                    searchAdapter.setData(filtered)
                    searchResultsRecyclerView.visibility = View.VISIBLE
                } else {
                    searchResultsRecyclerView.visibility = View.GONE
                    Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bottomSheet = view.findViewById(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetTitle = view.findViewById(R.id.bottomSheetTitle)
        bottomSheetAddress = view.findViewById(R.id.bottomSheetAddress)
        bottomSheetCity = view.findViewById(R.id.bottomSheetCity)
        bottomSheetPhone = view.findViewById(R.id.bottomSheetPhone)
        bottomSheetWorkingHours = view.findViewById(R.id.bottomSheetWorkingHours)
        bottomSheetDescription = view.findViewById(R.id.bottomSheetDescription)
        bottomSheetPhoto = view.findViewById(R.id.bottomSheetPhoto)

        myLocationFab = view.findViewById(R.id.myLocationFab)
        myLocationFab.setOnClickListener { moveCameraToUserLocation() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        if (places.isNotEmpty()) {
            val firstPlace = places[0]
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPlace.lat, firstPlace.lng), 10f))
        }

        addMarkers(places)

        googleMap.setOnMarkerClickListener { marker ->
            Log.d("MapFragment", "Marker clicked: ${marker.title}")
            val place = marker.tag as? Place
            if (place != null) {
                showPlaceDetails(place)
                true
            } else {
                false
            }
        }

        googleMap.setOnMapClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN }
    }

    private fun addMarkers(placesList: List<Place>) {
        googleMap.clear()
        for (place in placesList) {
            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.pin)
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 80, 80, false)

            val markerOptions = MarkerOptions()
                .position(LatLng(place.lat, place.lng))
                .title(place.name)
                .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = place
        }
    }

    private fun showPlaceDetails(place: Place) {
        bottomSheetTitle.text = place.name
        bottomSheetAddress.text = "Address: ${place.address}"
        bottomSheetCity.text = "City: ${place.city}"
        bottomSheetPhone.text = "Phone: ${place.phone}"
        bottomSheetWorkingHours.text = "Working Hours: ${place.workingHours}"
        bottomSheetDescription.text = "Description: ${place.description}"
        Picasso.get().load(place.photoUrl).into(bottomSheetPhoto)
        bottomSheet.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    private fun moveCameraToUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                } else {
                    Toast.makeText(requireContext(), "Current location not available", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    googleMap.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class PlaceSearchAdapter(
        private var places: List<Place>,
        private val onItemClick: (Place) -> Unit
    ) : RecyclerView.Adapter<PlaceSearchAdapter.PlaceViewHolder>() {

        inner class PlaceViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
            init {
                textView.setOnClickListener {
                    val place = places[adapterPosition]
                    onItemClick(place)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
            val textView = TextView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(16, 16, 16, 16)
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
            }
            return PlaceViewHolder(textView)
        }

        override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
            val place = places[position]
            holder.textView.text = "${place.name}, ${place.city}"
        }

        override fun getItemCount(): Int = places.size

        fun setData(newPlaces: List<Place>) {
            places = newPlaces
            notifyDataSetChanged()
        }
    }
}
