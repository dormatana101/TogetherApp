package com.example.togetherproject.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.togetherproject.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Picasso

class MapFragment : Fragment(), OnMapReadyCallback {

    data class Place(
        val name: String,
        val address: String,
        val city: String,
        val phone: String,
        val workingHours: String,
        val description: String,
        val photoUrl: String,
        val lat: Double,
        val lng: Double
    )

    private lateinit var googleMap: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheet: LinearLayout

    private lateinit var bottomSheetTitle: MaterialTextView
    private lateinit var bottomSheetAddress: MaterialTextView
    private lateinit var bottomSheetCity: MaterialTextView
    private lateinit var bottomSheetPhone: MaterialTextView
    private lateinit var bottomSheetWorkingHours: MaterialTextView
    private lateinit var bottomSheetDescription: MaterialTextView

    private lateinit var bottomSheetPhoto: com.google.android.material.imageview.ShapeableImageView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button

    private val places: List<Place> = listOf(
        Place(
            name = "Tel Aviv Volunteer Center",
            address = "Allenby St, Tel Aviv-Yafo",
            city = "Tel Aviv",
            phone = "+972 3-1234567",
            workingHours = "09:00-18:00",
            description = "A volunteer center supporting community initiatives in Tel Aviv.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.0853,
            lng = 34.7818
        ),
        Place(
            name = "Jerusalem Community Aid",
            address = "Jaffa Rd, Jerusalem",
            city = "Jerusalem",
            phone = "+972 2-2345678",
            workingHours = "08:00-17:00",
            description = "Providing assistance to families in need and promoting volunteer work in Jerusalem.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 31.7683,
            lng = 35.2137
        ),
        Place(
            name = "Haifa Social Support Hub",
            address = "Carmel Center, Haifa",
            city = "Haifa",
            phone = "+972 4-3456789",
            workingHours = "10:00-19:00",
            description = "A platform for volunteers offering social support in Haifa.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.7940,
            lng = 34.9896
        ),
        Place(
            name = "Eilat Volunteer Station",
            address = "Dolphin Reef, Eilat",
            city = "Eilat",
            phone = "+972 8-4567890",
            workingHours = "09:00-17:00",
            description = "Helping local communities and tourists in the southern resort city.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 29.5581,
            lng = 34.9482
        ),
        Place(
            name = "Netanya Helping Hands",
            address = "Herzl Blvd, Netanya",
            city = "Netanya",
            phone = "+972 3-5678901",
            workingHours = "08:30-18:00",
            description = "A center focused on volunteer activities in Netanya.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.3219,
            lng = 34.8530
        ),
        Place(
            name = "Ashdod Community Support",
            address = "Rambam St, Ashdod",
            city = "Ashdod",
            phone = "+972 8-6789012",
            workingHours = "09:00-17:30",
            description = "Supporting local families and communities in Ashdod through volunteer efforts.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 31.8014,
            lng = 34.6423
        ),
        Place(
            name = "Ashkelon Volunteer Network",
            address = "Herzl Ave, Ashkelon",
            city = "Ashkelon",
            phone = "+972 8-7890123",
            workingHours = "08:00-16:00",
            description = "Connecting volunteers with local projects in Ashkelon.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 31.6688,
            lng = 34.5743
        ),
        Place(
            name = "Be'er Sheva Volunteer Hub",
            address = "Ben Gurion St, Be'er Sheva",
            city = "Be'er Sheva",
            phone = "+972 8-8901234",
            workingHours = "09:00-18:00",
            description = "Fostering volunteer activities in the southern city of Be'er Sheva.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 31.2520,
            lng = 34.7913
        ),
        Place(
            name = "Rishon LeZion Outreach Center",
            address = "Herzl Blvd, Rishon LeZion",
            city = "Rishon LeZion",
            phone = "+972 3-9012345",
            workingHours = "08:00-17:00",
            description = "Offering outreach services and volunteer opportunities in Rishon LeZion.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.0840,
            lng = 34.8048
        ),
        Place(
            name = "Holon Volunteer Initiative",
            address = "Hashlosha St, Holon",
            city = "Holon",
            phone = "+972 3-0123456",
            workingHours = "09:00-17:00",
            description = "A local initiative engaging volunteers in various community projects.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.0114,
            lng = 34.7736
        ),
        Place(
            name = "Bat Yam Community Center",
            address = "Tchernichovsky St, Bat Yam",
            city = "Bat Yam",
            phone = "+972 3-1122334",
            workingHours = "08:30-17:30",
            description = "Providing volunteer services and community support in Bat Yam.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.0167,
            lng = 34.7500
        ),
        Place(
            name = "Petah Tikva Volunteer Office",
            address = "Hertzel St, Petah Tikva",
            city = "Petah Tikva",
            phone = "+972 3-2233445",
            workingHours = "09:00-18:00",
            description = "A hub for organizing volunteer activities in Petah Tikva.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.0841,
            lng = 34.8878
        ),
        Place(
            name = "Ramat Gan Community Services",
            address = "Jabotinsky St, Ramat Gan",
            city = "Ramat Gan",
            phone = "+972 3-3344556",
            workingHours = "09:00-18:00",
            description = "Connecting volunteers with community service projects in Ramat Gan.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.0689,
            lng = 34.8244
        ),
        Place(
            name = "Raanana Volunteer Center",
            address = "Ben Gurion St, Raanana",
            city = "Raanana",
            phone = "+972 3-4455667",
            workingHours = "08:00-17:00",
            description = "Promoting volunteer engagement and community support in Raanana.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.1880,
            lng = 34.9094
        ),
        Place(
            name = "Kfar Saba Outreach Hub",
            address = "Herzl St, Kfar Saba",
            city = "Kfar Saba",
            phone = "+972 3-5566778",
            workingHours = "09:00-17:00",
            description = "A center to support local volunteer projects in Kfar Saba.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.1700,
            lng = 34.8714
        ),
        Place(
            name = "Modi'in Volunteer Initiative",
            address = "Main St, Modi'in",
            city = "Modi'in",
            phone = "+972 3-6677889",
            workingHours = "09:00-18:00",
            description = "Engaging volunteers to improve local services in Modi'in.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 31.9030,
            lng = 35.0080
        ),
        Place(
            name = "Ramla Community Aid",
            address = "Main St, Ramla",
            city = "Ramla",
            phone = "+972 3-7788990",
            workingHours = "08:30-17:30",
            description = "Supporting community initiatives and volunteer programs in Ramla.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 31.9010,
            lng = 34.8750
        ),
        Place(
            name = "Lod Volunteer Office",
            address = "Ben Gurion St, Lod",
            city = "Lod",
            phone = "+972 3-8899001",
            workingHours = "09:00-18:00",
            description = "A center for organizing volunteer work and community outreach in Lod.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 31.9510,
            lng = 34.9080
        ),
        Place(
            name = "Hadera Community Center",
            address = "Main St, Hadera",
            city = "Hadera",
            phone = "+972 3-9900112",
            workingHours = "09:00-18:00",
            description = "Fostering volunteer activities and community projects in Hadera.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.4340,
            lng = 34.9240
        ),
        Place(
            name = "Herzliya Volunteer Hub",
            address = "Herzl Blvd, Herzliya",
            city = "Herzliya",
            phone = "+972 3-1011121",
            workingHours = "09:00-18:00",
            description = "A platform for volunteer initiatives in Herzliya.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.1663,
            lng = 34.8555
        ),
        Place(
            name = "Beit Shemesh Community Support",
            address = "Main St, Beit Shemesh",
            city = "Beit Shemesh",
            phone = "+972 3-1112131",
            workingHours = "08:00-17:00",
            description = "Volunteer support center aimed at assisting local communities in Beit Shemesh.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 31.7520,
            lng = 34.9880
        ),
        Place(
            name = "Nazareth Volunteer Initiative",
            address = "Haifa Rd, Nazareth",
            city = "Nazareth",
            phone = "+972 3-1213141",
            workingHours = "09:00-18:00",
            description = "A center promoting volunteer work to support the local community in Nazareth.",
            photoUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80",
            lat = 32.6996,
            lng = 35.3035
        )
    )

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
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            filterMarkers(query)
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

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

/*    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
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

        googleMap.setOnMapClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }*/


    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            checkLocationPermission()
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

        googleMap.setOnMapClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun addMarkers(placesList: List<Place>) {
        googleMap.clear()
        for (place in placesList) {
            val markerOptions = MarkerOptions()
                .position(LatLng(place.lat, place.lng))
                .title(place.name)
            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = place
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            googleMap.isMyLocationEnabled = true // If permission is already granted, enable location
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

    private fun filterMarkers(query: String) {
        val filtered = places.filter {
            it.name.contains(query, ignoreCase = true) || it.city.contains(query, ignoreCase = true)
        }
        addMarkers(filtered)
    }
/*

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
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
*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Check if permission is granted, and then enable location on the map
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.isMyLocationEnabled = true
                }
            } else {
                // Show a message when permission is denied
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }



}
