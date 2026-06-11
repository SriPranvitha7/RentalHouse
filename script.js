// ============================================================
// RentalHouse — script.js
// ============================================================

let map;
let markers = [];

// ── Load properties from localStorage ───────────────────────
function getProperties() {
  const saved = localStorage.getItem("hf_properties");
  return saved ? JSON.parse(saved) : [];
}

// ── Save properties to localStorage ─────────────────────────
function saveProperties(list) {
  localStorage.setItem("hf_properties", JSON.stringify(list));
}

// ── Google Maps callback ─────────────────────────────────────
function initMap() {
  map = new google.maps.Map(document.getElementById("map"), {
    zoom: 7,
    center: { lat: 17.3850, lng: 78.4867 },
    mapTypeControl: false,
    streetViewControl: false,
    fullscreenControl: true,
    styles: [
      { featureType: "poi", elementType: "labels", stylers: [{ visibility: "off" }] }
    ]
  });

  createMarkers(getProperties());
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
        <div style="font-family:'DM Sans',Arial,sans-serif; min-width:200px; padding:5px;">
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
      // Close all other info windows
      markers.forEach(m => { if (m.infoWindow) m.infoWindow.close(); });
      infoWindow.open(map, marker);
    });

    marker.infoWindow = infoWindow;
    markers.push(marker);
  });
}

// ── Search / filter properties ───────────────────────────────
function searchHouses() {
  const loc = document.getElementById("location").value.trim().toLowerCase();
  const type = document.getElementById("houseType").value;
  const rent = document.getElementById("rentRange").value;

  if (loc === "" && type === "" && rent === "") {
    alert("Please select at least one filter to search.");
    return;
  }

  const properties = getProperties();

  const filtered = properties.filter(property => {
    let matchLocation = true;
    let matchType = true;
    let matchRent = true;

    if (loc !== "") {
      matchLocation = property.location.toLowerCase().includes(loc);
    }
    if (type !== "") {
      matchType = property.type === type;
    }
    if (rent !== "") {
      const [min, max] = rent.split("-").map(Number);
      matchRent = property.rent >= min && property.rent <= max;
    }

    return matchLocation && matchType && matchRent;
  });

  createMarkers(filtered);

  // Pan map to searched location
  if (loc !== "") {
    fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(loc)}`)
      .then(response => response.json())
      .then(data => {
        if (data.length > 0) {
          map.setCenter({
            lat: parseFloat(data[0].lat),
            lng: parseFloat(data[0].lon)
          });
          map.setZoom(12);
        }
      })
      .catch(() => console.log("Location pan failed"));
  }
}

// ── Reset search ─────────────────────────────────────────────
function resetSearch() {
  document.getElementById("location").value = "";
  document.getElementById("houseType").value = "";
  document.getElementById("rentRange").value = "";

  const resultCount = document.getElementById("resultCount");
  if (resultCount) resultCount.style.display = "none";

  const noResults = document.getElementById("noResults");
  if (noResults) noResults.style.display = "none";

  createMarkers(getProperties());
  map.setCenter({ lat: 17.3850, lng: 78.4867 });
  map.setZoom(7);
}

// ── Add a new property ───────────────────────────────────────
function addProperty(event) {
  event.preventDefault();

  const owner    = document.getElementById("owner").value.trim();
  const phone    = document.getElementById("phone").value.trim();
  const location = document.getElementById("location").value.trim();
  const address  = document.getElementById("address").value.trim();
  const rent     = parseInt(document.getElementById("rent").value);
  const type     = document.getElementById("type").value;
  const lat      = parseFloat(document.getElementById("lat").value);
  const lng      = parseFloat(document.getElementById("lng").value);

  // Basic validation
  const errorMsg = document.getElementById("errorMessage");
  errorMsg.style.display = "none";

  if (!owner || !phone || !location || !address || !rent || !type || isNaN(lat) || isNaN(lng)) {
    errorMsg.textContent = "⚠️ Please fill in all fields correctly.";
    errorMsg.style.display = "block";
    return;
  }

  if (phone.length < 10) {
    errorMsg.textContent = "⚠️ Please enter a valid 10-digit phone number.";
    errorMsg.style.display = "block";
    return;
  }

  if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
    errorMsg.textContent = "⚠️ Please enter valid latitude and longitude values.";
    errorMsg.style.display = "block";
    return;
  }

  const newProperty = { owner, phone, location, address, rent, type, lat, lng };

  const properties = getProperties();
  properties.push(newProperty);
  saveProperties(properties);

  // Show success
  const successMsg = document.getElementById("successMessage");
  successMsg.style.display = "block";

  // Reset all fields
  ["owner","phone","location","address","rent","type","lat","lng"].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });

  // Scroll to top of form
  successMsg.scrollIntoView({ behavior: "smooth" });
}
