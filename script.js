// ============================================================
// HouseFinder — script.js
// ============================================================

const API = "http://localhost:4567/api/properties";

let map;
let markers = [];

// ── Google Maps callback ─────────────────────────────────────
function initMap() {
  map = new google.maps.Map(document.getElementById("map"), {
    zoom: 7,
    center: { lat: 17.3850, lng: 78.4867 },
    mapTypeControl: false,
    streetViewControl: false,
    fullscreenControl: true
  });

  // Load all properties from backend when map loads
  fetchAndShowAll();
}

// ── Fetch all properties from backend and show on map ────────
function fetchAndShowAll() {
  fetch(API)
    .then(res => res.json())
    .then(data => createMarkers(data))
    .catch(err => console.log("Backend error:", err));
}

// ── Create map markers ───────────────────────────────────────
function createMarkers(list) {
  // Clear old markers
  markers.forEach(marker => marker.setMap(null));
  markers = [];

  const noResults = document.getElementById("noResults");
  const resultCount = document.getElementById("resultCount");

  if (list.length === 0) {
    if (noResults) noResults.style.display = "block";
    if (resultCount) resultCount.style.display = "none";
    return;
  }

  if (noResults) noResults.style.display = "none";
  if (resultCount) {
    resultCount.style.display = "block";
    resultCount.textContent = `Found ${list.length} propert${list.length === 1 ? "y" : "ies"}`;
  }

  list.forEach(property => {
    const marker = new google.maps.Marker({
      position: { lat: property.lat, lng: property.lng },
      map: map,
      title: property.type + " - " + property.location,
      animation: google.maps.Animation.DROP
    });

    const infoWindow = new google.maps.InfoWindow({
      content: `
        <div style="font-family:Arial,sans-serif; min-width:200px; padding:5px;">
          <h3 style="margin:0 0 8px; color:#1e3a8a; font-size:16px;">${property.type}</h3>
          <p style="margin:4px 0;">👤 <strong>Owner:</strong> ${property.owner}</p>
          <p style="margin:4px 0;">📞 <strong>Phone:</strong> ${property.phone || "Not available"}</p>
          <p style="margin:4px 0;">💰 <strong>Rent:</strong> ₹${Number(property.rent).toLocaleString()}/month</p>
          <p style="margin:4px 0;">📍 <strong>Location:</strong> ${property.location}</p>
          ${property.address ? `<p style="margin:4px 0;">🏠 <strong>Address:</strong> ${property.address}</p>` : ""}
        </div>
      `
    });

    marker.addListener("click", () => {
      markers.forEach(m => { if (m.infoWindow) m.infoWindow.close(); });
      infoWindow.open(map, marker);
    });

    marker.infoWindow = infoWindow;
    markers.push(marker);
  });
}

// ── Search properties ────────────────────────────────────────
function searchHouses() {
  const loc  = document.getElementById("location").value.trim();
  const type = document.getElementById("houseType").value;
  const rent = document.getElementById("rentRange").value;

  if (loc === "" && type === "" && rent === "") {
    alert("Please select at least one filter.");
    return;
  }

  // Build query URL
  let url = API + "?";
  if (loc)  url += "location=" + encodeURIComponent(loc) + "&";
  if (type) url += "type=" + encodeURIComponent(type) + "&";
  if (rent) url += "rent=" + encodeURIComponent(rent);

  fetch(url)
    .then(res => res.json())
    .then(data => {
      createMarkers(data);

      // Pan map to searched location
      if (loc !== "") {
        fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(loc)}`)
          .then(r => r.json())
          .then(geo => {
            if (geo.length > 0) {
              map.setCenter({ lat: parseFloat(geo[0].lat), lng: parseFloat(geo[0].lon) });
              map.setZoom(12);
            }
          });
      }
    })
    .catch(err => console.log("Search error:", err));
}

// ── Reset search ─────────────────────────────────────────────
function resetSearch() {
  document.getElementById("location").value = "";
  document.getElementById("houseType").value = "";
  document.getElementById("rentRange").value = "";

  // Reset pills
  document.querySelectorAll(".pill").forEach(p => p.classList.remove("active"));
  document.querySelectorAll(".pill[data-value='']").forEach(p => p.classList.add("active"));

  const resultCount = document.getElementById("resultCount");
  if (resultCount) resultCount.style.display = "none";

  fetchAndShowAll();
  map.setCenter({ lat: 17.3850, lng: 78.4867 });
  map.setZoom(7);
}

// ── Add property — saves to MySQL via backend ────────────────
function addProperty(event) {
  event.preventDefault();

  const owner    = document.getElementById("owner").value.trim();
  const phone    = document.getElementById("phone").value.trim();
  const location = document.getElementById("location").value.trim();
  const address  = document.getElementById("address").value.trim();
  const rent     = document.getElementById("rent").value.trim();
  const type     = document.getElementById("type").value;
  const lat      = document.getElementById("lat").value.trim();
  const lng      = document.getElementById("lng").value.trim();

  const errorMsg   = document.getElementById("errorMessage");
  const successMsg = document.getElementById("successMessage");

  errorMsg.style.display = "none";
  successMsg.style.display = "none";

  // Validation
  if (!owner || !phone || !location || !address || !rent || !type || !lat || !lng) {
    errorMsg.textContent = "Please fill in all fields.";
    errorMsg.style.display = "block";
    return;
  }

  if (phone.length < 10) {
    errorMsg.textContent = "Please enter a valid 10-digit phone number.";
    errorMsg.style.display = "block";
    return;
  }

  // Build form data string
  const body = `owner=${encodeURIComponent(owner)}`
    + `&phone=${encodeURIComponent(phone)}`
    + `&location=${encodeURIComponent(location)}`
    + `&address=${encodeURIComponent(address)}`
    + `&rent=${encodeURIComponent(rent)}`
    + `&type=${encodeURIComponent(type)}`
    + `&lat=${encodeURIComponent(lat)}`
    + `&lng=${encodeURIComponent(lng)}`;

  // Send to backend
  fetch(API, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: body
  })
    .then(res => res.json())
    .then(data => {
      if (data.status === "success") {
        successMsg.style.display = "block";
        // Clear all fields
        ["owner","phone","location","address","rent","type","lat","lng"]
          .forEach(id => document.getElementById(id).value = "");
        successMsg.scrollIntoView({ behavior: "smooth" });
      } else {
        errorMsg.textContent = data.message || "Something went wrong.";
        errorMsg.style.display = "block";
      }
    })
    .catch(err => {
      errorMsg.textContent = "Cannot connect to server. Make sure backend is running.";
      errorMsg.style.display = "block";
    });
}
