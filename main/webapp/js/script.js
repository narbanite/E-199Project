
let lat = null;
let lon = null;
let globalUsername;



function createMap() {
    const map = new OpenLayers.Map("Map");
    const mapnik = new OpenLayers.Layer.OSM();
    map.addLayer(mapnik);
    const markers = new OpenLayers.Layer.Markers("Markers");
    map.addLayer(markers);
    return { map, markers };
}

const { map, markers } = createMap();
const zoom = 11;


// load Google Charts library
google.charts.load('current', { packages: ['corechart'] });
google.charts.setOnLoadCallback(() => {
    document.getElementById('view-statistics-btn').addEventListener('click', viewStatistics);
});

function validatePasswords() {
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;
    const errorDiv = document.getElementById("password-error");
    const passwordStrength = document.getElementById("password-strength");

    //clear previous messages
    errorDiv.textContent = "";
    passwordStrength.textContent = "";

    //check if passwords match
    if (password !== confirmPassword) {
        errorDiv.textContent = "Οι κωδικοί δεν ταιριάζουν.";
        return false; 
    }

    //prohibited words check
    const prohibitedWords = ["fire", "fotia", "ethelontis", "volunteer"];
    for (const word of prohibitedWords) {
        if (password.toLowerCase().includes(word)) {
            errorDiv.textContent = "Το password περιέχει απαγορευμένες λέξεις.";
            return false;
        }
    }

    //weak password checks
    let digitCount = 0;
    let repeatedCharCount = {};
    for (let char of password) {
        if (/\d/.test(char)) digitCount++;
        repeatedCharCount[char] = (repeatedCharCount[char] || 0) + 1;
    }
    const halfLength = password.length / 2;
    const maxRepeatedCharCount = Math.max(...Object.values(repeatedCharCount));

    if (digitCount >= halfLength || maxRepeatedCharCount >= halfLength) {
        errorDiv.textContent = "Weak password.";
        return false;
    }

    //strong and medium password check
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasNumber = /\d/.test(password);
    const hasSymbol = /[@$!%*?&]/.test(password);

    if (hasUpper && hasLower && hasNumber && hasSymbol) {
        passwordStrength.textContent = "Strong password.";
    } else {
        passwordStrength.textContent = "Medium password.";
    }

    return true; //allow form submission if no errors
}

function checkAgeForVolunteerFirefighter() {
    const userType = document.querySelector('input[name="type"]:checked').value;
    const birthdate = document.getElementById("birthdate").value;
    const errorDiv = document.getElementById("password-error");

    //check age only for volunteer firefighters
    if (userType === "volunteer_firefighter") {
        const birthYear = new Date(birthdate).getFullYear();
        const currentYear = new Date().getFullYear();
        const age = currentYear - birthYear;

        if (age < 18 || age > 55) {
            errorDiv.textContent = "Η ηλικία πρέπει να είναι μεταξύ 18 και 55 ετών για Εθελοντές Πυροσβέστες.";
            return false;
        }
    }
    return true;
}

function handleFormSubmission() {
    //run all validations
    if (!validatePasswords() || !checkAgeForVolunteerFirefighter()) {
        return false; 
    }

    //put all data inside an object
    const formData = {
        username: document.getElementById("username").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
        firstname: document.getElementById("firstname").value,
        lastname: document.getElementById("lastname").value,
        birthdate: document.getElementById("birthdate").value,
        gender: document.querySelector('input[name="gender"]:checked').value,
        afm: document.getElementById("afm").value,
        country: document.getElementById("country").value,
        prefecture: document.getElementById("prefecture").value,
        municipality: document.getElementById("municipality").value,
        address: document.getElementById("address").value,
        telephone: document.getElementById("telephone").value,
        job: document.getElementById("job").value,
        type: document.querySelector('input[name="type"]:checked').value,
        terms: document.getElementById("terms").checked,
        lat: window.lat,
        lon: window.lon
    };

    //add the extra fields if the user is a volunteer firefighter
    if (formData.type === "volunteer_firefighter") {
        formData.volunteer_type = document.getElementById("volunteer_type").value;
        formData.height = document.getElementById("height").value;
        formData.weight = document.getElementById("weight").value;
    }
    //convert data to json
    const jsonOutput = document.getElementById("json-output");
    jsonOutput.textContent = JSON.stringify(formData, null, 2);
    
    const endpoint =
        formData.type === "volunteer_firefighter"
            ? "http://localhost:8080/ServletWithDatabaseConnection2024_2025_Maven/RegistrationVolunteer"
            : "http://localhost:8080/ServletWithDatabaseConnection2024_2025_Maven/RegistrationUser";
    
    fetch(endpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(formData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert("Error: " + data.error);
        } else {
            alert(data.message);
        }
    })
    .catch(error => {
        alert("An error occurred: " + error.message);
    });
     
    

    return false;
}

function togglePasswordVisibility() {
    const passwordField = document.getElementById("password");
    const confirmPasswordField = document.getElementById("confirm-password");

    if (passwordField.type === "password") {
        passwordField.type = "text";
        confirmPasswordField.type = "text";
    } else {
        passwordField.type = "password";
        confirmPasswordField.type = "password";
    }
}

//very similar to validatePasswords()
function checkPasswordStrength() {
    const password = document.getElementById("password").value;
    const passwordStrength = document.getElementById("password-strength");

    passwordStrength.textContent = "";

    //prohibited words check
    const prohibitedWords = ["fire", "fotia", "ethelontis", "volunteer"];
    for (const word of prohibitedWords) {
        if (password.toLowerCase().includes(word)) {
            passwordStrength.textContent = "Το password περιέχει απαγορευμένες λέξεις.";
            return;
        }
    }

    //weak password checks
    let digitCount = 0;
    let repeatedCharCount = {};
    for (let char of password) {
        if (/\d/.test(char)) digitCount++;
        repeatedCharCount[char] = (repeatedCharCount[char] || 0) + 1;
    }
    const halfLength = password.length / 2;
    const maxRepeatedCharCount = Math.max(...Object.values(repeatedCharCount));

    if (digitCount >= halfLength || maxRepeatedCharCount >= halfLength) {
        passwordStrength.textContent = "Weak password.";
        return;
    }

    //strong and medium password check
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasNumber = /\d/.test(password);
    const hasSymbol = /[@$!%*?&]/.test(password);

    if (hasUpper && hasLower && hasNumber && hasSymbol) {
        passwordStrength.textContent = "Strong password.";
    } else {
        passwordStrength.textContent = "Medium password.";
    }
}

function verifyAddress() {
    const street = document.getElementById("address").value; 
    const city = document.getElementById("municipality").value; 
    const country = document.getElementById("country").value;

    const url = `https://forward-reverse-geocoding.p.rapidapi.com/v1/forward?format=json` +
                `&street=${encodeURIComponent(street)}` +
                `&city=${encodeURIComponent(city)}` +
                `&country=${encodeURIComponent(country)}` +
                `&addressdetails=1&accept-language=en&limit=1`;

    //RAPID API!
    const data = null;
    const xhr = new XMLHttpRequest();
    xhr.withCredentials = true;

    xhr.addEventListener('readystatechange', function () {
        if (this.readyState === this.DONE) {
            try {
                //parse the JSON response
                const response = JSON.parse(this.responseText);

                //check if the response is empty
                if (response.length === 0) {
                    document.getElementById("address-error").textContent = "Η τοποθεσία δεν βρέθηκε. Παρακαλώ ελέγξτε τη διεύθυνσή σας.";
                    document.getElementById("Map").style.display = "none"; 
                    return;
                }

                
                const location = response[0];
                const displayName = location.display_name;
               
                window.lat = parseFloat(location.lat);
                window.lon = parseFloat(location.lon);

                
                if (!displayName.toLowerCase().includes("crete")) {
                    document.getElementById("address-error").textContent = "Η υπηρεσία είναι διαθέσιμη μόνο για τοποθεσίες στην Κρήτη.";
                    document.getElementById("Map").style.display = "none";
                    return;
                }

                
                document.getElementById("address-error").textContent = "Η τοποθεσία επαληθεύτηκε επιτυχώς.";
                console.log("Latitude:", window.lat);
                console.log("Longitude:", window.lon);

                document.getElementById("Map").style.display = "block"; 
                clearMarkers(); 
                centerMap(window.lat, window.lon); 
                addMarker(window.lat, window.lon, "Verified Location");


            } catch (error) {
                document.getElementById("address-error").textContent = "Παρουσιάστηκε σφάλμα κατά την επεξεργασία των δεδομένων.";
                document.getElementById("Map").style.display = "none";
                console.error("Error:", error);
            }
        }
    });

    
    xhr.open('GET', url);
    
    xhr.setRequestHeader('x-rapidapi-key', 'f3cbf2db72mshdc22730fbbe08b9p169dfajsna303473fae5c'); 
    xhr.setRequestHeader('x-rapidapi-host', 'forward-reverse-geocoding.p.rapidapi.com');

    xhr.send(data);

}

function setPosition(lat, lon) {
    var fromProjection = new OpenLayers.Projection("EPSG:4326"); // WGS 1984
    var toProjection = new OpenLayers.Projection("EPSG:900913"); // Spherical Mercator
    return new OpenLayers.LonLat(lon, lat).transform(fromProjection, toProjection);
}

function clearMarkers() {
    if (!markers) {
        console.error("Markers layer is not initialized.");
        return;
    }
   
    markers.clearMarkers();
    map.popups.forEach(popup => map.removePopup(popup));
}


function addMarker(lat, lon, message) {
    var position = setPosition(lat, lon);
    var marker = new OpenLayers.Marker(position);
    markers.addMarker(marker);

    marker.events.register("mousedown", marker, function(evt) {
        var popup = new OpenLayers.Popup.FramedCloud(
            "Popup",
            position,
            null,
            message,
            null,
            true 
        );
        map.addPopup(popup);
    });
}

function centerMap(lat, lon) {
    var position = setPosition(lat, lon);
    map.setCenter(position, zoom);
}


function showVolunteerFields() {
    const volunteerFields = document.getElementById("volunteer-fields");
    const termsLabel = document.getElementById("terms");

    //show the extra fields for volunteer firefighters
    volunteerFields.style.display = "block";

    //update the terms message
    termsLabel.textContent = "Συμφωνώ με τους όρους χρήσης: Δηλώνω υπεύθυνα ότι ανήκω στο ενεργό δυναμικό των εθελοντών πυροσβεστών.";
}


function toggleVolunteerFields(show) {
    const volunteerFields = document.getElementById("volunteer-fields");
    const termsLabel = document.getElementById("terms-label");

    if (show) {
        
        volunteerFields.style.display = "block";
        termsLabel.textContent = "Συμφωνώ με τους όρους χρήσης: Δηλώνω υπεύθυνα ότι ανήκω στο ενεργό δυναμικό των εθελοντών πυροσβεστών.";

        
        document.getElementById("volunteer_type").setAttribute("required", "true");
        document.getElementById("height").setAttribute("required", "true");
        document.getElementById("weight").setAttribute("required", "true");
    } else {
        
        volunteerFields.style.display = "none";
        termsLabel.textContent = "Συμφωνώ με τους όρους χρήσης:";

        
        document.getElementById("volunteer_type").removeAttribute("required");
        document.getElementById("height").removeAttribute("required");
        document.getElementById("weight").removeAttribute("required");
    }
}

function loginPOST() {
    let username = document.getElementById('username_log').value;
    let password = document.getElementById('password_log').value;

    console.log("Username:", username, "Password:", password);

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'Login', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                try {
                    const response = JSON.parse(xhr.responseText);
                    if (response.success) {
                        const userType = response.userType; 
                        sessionStorage.setItem('globalUsername', username);

                        if (username === 'admin' && password === 'admiN12@*') {
                            sessionStorage.setItem('userType', 'admin');
                            window.location.href = 'admin.html';
                        } else if (userType === 'volunteer') {
                            const volunteerType = response.volunteerType;
                            sessionStorage.setItem('userType', 'volunteer');
                            sessionStorage.setItem('volunteerType', volunteerType);
                            window.location.href = 'volunteer.html'; // redirect to volunteer page
                        } else if (userType === 'user') {
                            sessionStorage.setItem('userType', 'user');
                            window.location.href = 'user.html'; // redirect to user page
                        } else {
                            console.log("Unknown user type");
                        }
                    } else {
                        alert("Wrong credentials, please try again.");
                    }
                } catch (error) {
                    console.error("Error parsing response:", error);
                    alert("An error occurred. Please try again.");
                }
            } else {
                alert("Login failed. Please check your credentials.");
            }
        }
    };

    const data = `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;
    console.log("Sending request with data:", data);
    xhr.send(data);
}


//function logout(){
//    var xhr = new XMLHttpRequest();
//    xhr.onload = function () {
//        if (xhr.readyState === 4 && xhr.status === 200) {
//            $('#choices').load("buttons.html");
//            $("#ajaxContent").html("Successful Logout");
//            //redirect back 
//            window.location.href = "index.html";
//        } else if (xhr.status !== 200) {
//            alert('Request failed. Returned status of ' + xhr.status);
//        }
//    };
//    xhr.open('POST', 'Logout');
//    xhr.setRequestHeader('Content-type','application/x-www-form-urlencoded');
//    xhr.send();
//}

function UpdatePOST() {
    let myForm = document.getElementById('editForm');
    let formData = new FormData(myForm);
    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const responseData = JSON.parse(xhr.responseText);
                $('#ajaxContent').html("Successful Registration. Now please log in!<br> Your Data");
                $('#ajaxContent').append(createTableFromJSON(responseData));
            } else {
                $('#ajaxContent').html('Request failed. Returned status of ' + xhr.status + "<br>");
                if (xhr.responseText) {
                    const responseData = JSON.parse(xhr.responseText);
                    for (const x in responseData) {
                        $('#ajaxContent').append("<p style='color:red'>" + x + "=" + responseData[x] + "</p>");
                    }
                } else {
                    $('#ajaxContent').append("<p>There was an error processing your request.</p>");
                }
            }
        }
    };

    xhr.onerror = function () {
        alert("Network Error. Please try again.");
    };
    
    const data = {};
    formData.forEach((value, key) => {
        if (value.trim() !== "") {
            data[key] = value; //include only non-empty fields
        }
    });
    
    const jsonData = JSON.stringify(data);
    console.log(jsonData);
    xhr.open('POST', 'UpdateUser');
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.send(jsonData);
}


function loadUser() {
    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log("Response data:", xhr.responseText);
            try {
                const parsedResponse = JSON.parse(xhr.responseText);
                console.log("Parsed response:", parsedResponse);

                if (parsedResponse.length > 0) {
                    const user = parsedResponse[0]; 
                    const tableContent = createUserTableJSON(user); 
                    populateForm(user);
                    document.getElementById('userContent').innerHTML = tableContent;
                } else {
                    document.getElementById('userContent').innerHTML = '<p>No user data available.</p>';
                }
            } catch (error) {
                console.error("JSON parsing error:", error);
                document.getElementById('userContent').innerHTML = '<p>Invalid JSON response.</p>';
            }
        } else {
            console.error("Request failed with status:", xhr.status);
            document.getElementById('userContent').innerHTML = 'Request failed. Returned status of ' + xhr.status;
        }
    };
    xhr.onerror = function () {
        alert("Network Error. Please try again.");
    };
    xhr.open('GET', 'LoadUser');
    xhr.send();
}



function populateForm(user) {
    //loop through the user object keys
    for (const key in user) {
        //check if an input field with the corresponding ID exists
        const field = document.getElementById(key);
        if (field) {
            //if the field exists, set its value
            field.value = user[key];
        }
    }
}



function updateUserInfo() {
    var formData = new FormData(document.getElementById('userForm'));
    var updatedData = {};

    //collect only modified fields
    formData.forEach((value, key) => {
        if (value.trim() !== "") {
            updatedData[key] = value;
        }
    });
    
   
    const lat = parseFloat(document.getElementById('user_lat').value.trim()) || 0.0;
    const lon = parseFloat(document.getElementById('user_lon').value.trim()) || 0.0;

    updatedData.lat = lat;
    updatedData.lon = lon;
    
    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'UpdateUser'); 
    xhr.setRequestHeader('Content-type', 'application/json');
    xhr.onload = function () {
        if (xhr.status === 200) {
            alert("Your information has been updated successfully!");
        } else {
            alert("Failed to update your information. Please try again.");
        }
    };
    xhr.onerror = function () {
        alert("Network error. Please try again.");
    };

    xhr.send(JSON.stringify(updatedData));
}


function logout(){
    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            $('#choices').load("buttons.html");
            $("#ajaxContent").html("Successful Logout");
            //redirect back 
            window.location.href = "index.html";
        } else if (xhr.status !== 200) {
            alert('Request failed. Returned status of ' + xhr.status);
        }
    };
    xhr.open('POST', 'Logout');
    xhr.setRequestHeader('Content-type','application/x-www-form-urlencoded');
    xhr.send();
}

function loadIncidents() {
    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log("Response data:", xhr.responseText);
            try {
                const parsedResponse = JSON.parse(xhr.responseText);
                console.log("Parsed response:", parsedResponse);
                let tableContent = loadIncidentTableJSON(parsedResponse);
                document.getElementById('incidentContent').innerHTML = tableContent;
            } catch (error) {
                console.error("Error parsing JSON:", error);
                document.getElementById('incidentContent').innerHTML = '<p>Invalid JSON response.</p>';
            }
        } else {
            console.error("Request failed with status:", xhr.status);
            document.getElementById('incidentContent').innerHTML = 'Request failed. Returned status of ' + xhr.status;
        }
    };
    xhr.onerror = function () { 
        alert("Network Error. Please try again."); 
    };
    xhr.open('GET', 'LoadIncidents'); 
    xhr.send();
}

function loadIncidentsUser() {
    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log("Response data:", xhr.responseText);
            try {
                const parsedResponse = JSON.parse(xhr.responseText);
                console.log("Parsed response:", parsedResponse);
                let tableContent = loadIncidentTableUserJSON(parsedResponse);
                document.getElementById('userContent').innerHTML = tableContent;
            } catch (error) {
                console.error("Error parsing JSON:", error);
                document.getElementById('userContent').innerHTML = '<p>Invalid JSON response.</p>';
            }
        } else {
            console.error("Request failed with status:", xhr.status);
            document.getElementById('userContent').innerHTML = 'Request failed. Returned status of ' + xhr.status;
        }
    };
    xhr.onerror = function () { 
        alert("Network Error. Please try again."); 
    };
    xhr.open('GET', 'LoadIncidentsUser'); 
    xhr.send();
}

function loadIncidentsGuest() {
    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log("Response data:", xhr.responseText);
            try {
                const parsedResponse = JSON.parse(xhr.responseText);
                console.log("Parsed response:", parsedResponse);
                let tableContent = loadIncidentTableGuestJSON(parsedResponse); 
                document.getElementById('guestContent').innerHTML = tableContent;
            } catch (error) {
                console.error("Error parsing JSON:", error);
                document.getElementById('guestContent').innerHTML = '<p>Invalid JSON response.</p>';
            }
        } else {
            console.error("Request failed with status:", xhr.status);
            document.getElementById('guestContent').innerHTML = 'Request failed. Returned status of ' + xhr.status;
        }
    };
    xhr.onerror = function () { 
        alert("Network Error. Please try again."); 
    };
    xhr.open('GET', 'LoadIncidentsUser'); 
    xhr.send();
}

function submitIncident() {
    let myForm = document.getElementById('incidentForm');
    let formData = new FormData(myForm);
        
    const data = {};
    formData.forEach((value, key) => {
        //remove 'new_' from each key
        let modifiedKey = key.replace('new_', '');
        data[modifiedKey] = value.trim();
    });
    
    var jsonData = JSON.stringify(data);
    console.log(jsonData);

    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                document.getElementById('response').innerHTML = 'Incident added successfully';
            } else {
                handleAPIError(xhr);
            }
        }
    };
    
    xhr.onerror = function () {
        alert("Failed to connect to the server.");
    };

    xhr.open('POST', ' http://localhost:4567/incidentsAPI/newIncident');
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.send(jsonData);
}

function submitIncidentAdmin() {
    let myForm = document.getElementById('addIncidentForm');
    let formData = new FormData(myForm);

    console.log("AJAX: Submitting Incident");

    const data = {};
    formData.forEach((value, key) => {
        data[key] = value.trim() || null; //set null for empty fields
    });

    const jsonData = JSON.stringify(data);
    console.log("Payload:", jsonData);

    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const contentType = xhr.getResponseHeader("Content-Type");
                if (contentType && contentType.startsWith("application/json")) {
                    const responseData = JSON.parse(xhr.responseText);
                    alert("Incident added successfully!");
                    console.log("Response Data:", responseData);
                    myForm.reset();//clear the form
                } else {
                    console.error("Unexpected response:", xhr.responseText);
                }
            } else {
                console.error(`Request failed. Status: ${xhr.status}`, xhr.responseText);
                alert("Failed to add the incident. Check the console for details.");
            }
        }
    };

    xhr.onerror = function () {
        alert("Network Error. Please try again.");
    };

    xhr.open('POST', 'AddIncident');
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.send(jsonData);
}







function selectIncident(incidentId) {
    console.log(`Incident ID: ${incidentId}`);
    sessionStorage.setItem('selectedIncidentID', incidentId); 

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'GetIncidentDetails', true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    const requestData = JSON.stringify({
        incident_id: incidentId
    });

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const response = JSON.parse(xhr.responseText);
                console.log('Response from servlet:', response);

                //handle missing or null datetime fields
                const startDatetime = response.start_datetime ? response.start_datetime.replace(" ", "T") : "";
                const endDatetime = response.end_datetime ? response.end_datetime.replace(" ", "T") : "";

                //populate the form with the incident details
                document.getElementById("incident_id").value = response.incident_id || "";
                document.getElementById("incident_type").value = response.incident_type || "";
                document.getElementById("description").value = response.description || "";
                document.getElementById("user_phone").value = response.user_phone || "";
                document.getElementById("address").value = response.address || "";
                document.getElementById("lat").value = response.lat || "";
                document.getElementById("lon").value = response.lon || "";
                document.getElementById("municipality").value = response.municipality || "";
                document.getElementById("prefecture").value = response.prefecture || "";
                document.getElementById("start_datetime").value = startDatetime;
                document.getElementById("end_datetime").value = endDatetime;
                document.getElementById("danger").value = response.danger || "";
                document.getElementById("status").value = response.status || "";
                document.getElementById("finalResult").value = response.finalResult || "";
                document.getElementById("vehicles").value = response.vehicles || 0;
                document.getElementById("firemen").value = response.firemen || 0;

                // show the form incidents
                document.getElementById("incidentFormContainer").style.display = "block";
                
                //show the message form
                if (response.status === "running") {
                    document.getElementById('message_incident_id').value = response.incident_id;
                    document.getElementById('messageFormContainer').style.display = 'block';
                    document.getElementById('addSeatsFormContainer').style.display = 'block';
                } else {
                    //hide the message form if the status is not 'running'
                    document.getElementById('messageFormContainer').style.display = 'none';
                    document.getElementById('addSeatsFormContainer').style.display = 'none';
                }
            } else {
                console.error('Error:', xhr.responseText);
            }
        }
    };


    xhr.send(requestData);
}

function selectIncidentUser(incidentId) {
    console.log(`Incident ID: ${incidentId}`);
    sessionStorage.setItem('selectedIncidentID', incidentId); 

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'GetIncidentDetails', true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    const requestData = JSON.stringify({
        incident_id: incidentId
    });

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const response = JSON.parse(xhr.responseText);
                console.log('Response from servlet:', response);

                
                const messageIncidentIdField = document.getElementById('message_incident_id');
                const messageFormContainer = document.getElementById('userMessageFormContainer');

                if (messageIncidentIdField && messageFormContainer) {
                    messageIncidentIdField.value = response.incident_id || "";
                    messageFormContainer.style.display = 'block'; 
                } else {
                    console.error("Message form elements not found in the DOM.");
                }
            } else {
                console.error('Error:', xhr.responseText);
            }
        }
    };

    xhr.send(requestData);
}


function sendPublicMessage() {
    const incidentId = document.getElementById('message_incident_id').value;
    const sender = document.getElementById('message_sender').value;
    const recipient = document.getElementById('message_recipient').value;
    const messageText = document.getElementById('message_text').value;

    let recipientType = null;
    let recipientId = recipient; 

    if (recipient === 'Other') {
        recipientType = document.getElementById('recipient_type').value;
        recipientId = document.getElementById('recipient_id').value;

        if (!recipientType || !recipientId) {
            alert('Please select a valid recipient type and enter a recipient ID.');
            return;
        }
    }

    const requestData = {
        incident_id: incidentId,
        sender: sender,
        recipient: recipientId, 
        message_text: messageText,
        recipient_type: recipientType 
    };

    console.log("Sending Message:", requestData);

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'SendPublicMessage', true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                alert("Message sent successfully!");
                document.getElementById('messageForm').reset(); //reset the form
                document.getElementById('recipientOptions').style.display = 'none'; //hide recipient options
            } else {
                console.error('Error:', xhr.responseText);
                alert('Failed to send the message.');
            }
        }
    };

    xhr.onerror = function () {
        alert("Network Error. Please try again.");
    };

    xhr.send(JSON.stringify(requestData));
}


function toggleRecipientOptions() {
    const recipient = document.getElementById('message_recipient').value;
    const recipientOptions = document.getElementById('recipientOptions');
    const recipientIdLabel = document.getElementById('recipientIdLabel');
    const recipientIdInput = document.getElementById('recipient_id');

    if (recipient === 'Other') {
        recipientOptions.style.display = 'block'; 
    } else {
        recipientOptions.style.display = 'none'; 
        recipientIdLabel.style.display = 'none'; 
        recipientIdInput.style.display = 'none'; 
        recipientIdInput.value = ''; 
    }
}

function toggleRecipientIdField() {
    const recipientType = document.getElementById('recipient_type').value;
    const recipientIdLabel = document.getElementById('recipientIdLabel');
    const recipientIdInput = document.getElementById('recipient_id');

    if (recipientType === 'Volunteer' || recipientType === 'User') {
        recipientIdLabel.style.display = 'inline'; 
        recipientIdInput.style.display = 'inline'; 
        recipientIdInput.placeholder = `Enter ${recipientType} username`;
    } else {
        recipientIdLabel.style.display = 'none'; 
        recipientIdInput.style.display = 'none'; 
        recipientIdInput.value = ''; 
    }
}



function updateIncident() {
   
    const updatedIncident = {
        incident_id: document.getElementById("incident_id").value.trim(),
        incident_type: document.getElementById("incident_type").value.trim() || null,
        description: document.getElementById("description").value.trim() || null,
        user_phone: document.getElementById("user_phone").value.trim() || null,
        user_type: document.getElementById("user_type").value.trim() || null,
        address: document.getElementById("address").value.trim() || null,
        lat: document.getElementById("lat").value ? parseFloat(document.getElementById("lat").value) : null,
        lon: document.getElementById("lon").value ? parseFloat(document.getElementById("lon").value) : null,
        municipality: document.getElementById("municipality").value.trim() || null,
        prefecture: document.getElementById("prefecture").value.trim() || null,
        start_datetime: document.getElementById("start_datetime").value 
            ? document.getElementById("start_datetime").value.replace("T", " ") 
            : null,
        end_datetime: document.getElementById("end_datetime").value 
            ? document.getElementById("end_datetime").value.replace("T", " ") 
            : null,
        danger: document.getElementById("danger").value.trim() || null,
        status: document.getElementById("status").value.trim() || null,
        finalResult: document.getElementById("finalResult").value.trim() || null,
        vehicles: document.getElementById("vehicles").value ? parseInt(document.getElementById("vehicles").value) : null,
        firemen: document.getElementById("firemen").value ? parseInt(document.getElementById("firemen").value) : null
    };
    
    
    if (updatedIncident.status === "finished") {
        const currentDateTime = new Date().toISOString().slice(0, 19).replace("T", " ");
        updatedIncident.end_datetime = currentDateTime;
        if (!updatedIncident.finalResult || updatedIncident.finalResult.trim() === "") {
            updatedIncident.finalResult = "finished guys! no need to worry no more.";
        }
    }
    
    Object.keys(updatedIncident).forEach(key => {
        if (updatedIncident[key] === null || updatedIncident[key] === "") {
            delete updatedIncident[key];
        }
    });

    console.log("Sending updated incident:", updatedIncident);

    
    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'UpdateIncident', true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                alert("Incident updated successfully!");
                loadIncidents(); //reload incidents or refresh the table
            } else {
                const errorResponse = xhr.responseText ? JSON.parse(xhr.responseText) : {};
                alert("Failed to update the incident: " + (errorResponse.error || "Unknown error"));
                console.error("Error response:", errorResponse);
            }
        }
    };

    xhr.onerror = function () {
        alert("Network error. Could not connect to the server.");
    };

    
    xhr.send(JSON.stringify(updatedIncident));

    
    document.getElementById("incidentFormContainer").style.display = "none";
}




function viewMessages() {
    const incidentContent = document.getElementById('incidentContent');
    const placeholder = document.getElementById('placeholder');

    
    incidentContent.innerHTML = '<p>Loading messages...</p>';
    if (placeholder) placeholder.style.display = 'none';
    
    fetch('GetMessages', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error('Failed to load messages');
            }
            return response.json();
        })
        .then((messages) => {
            const incidentContent = document.getElementById('incidentContent');
            incidentContent.innerHTML = ''; 

            if (messages.length === 0) {
                incidentContent.innerHTML = '<p>No messages found.</p>';
                return;
            }

            messages.forEach((message) => {
                const messageDiv = document.createElement('div');
                messageDiv.classList.add('message-item');

                messageDiv.innerHTML = `
                    <p><strong>Message ID:</strong> ${message.message_id}</p>
                    <p><strong>Incident ID:</strong> ${message.incident_id}</p>
                    <p><strong>Message:</strong> ${message.message}</p>
                    <p><strong>Sender:</strong> ${message.sender}</p>
                    <p><strong>Recipient:</strong> ${message.recipient}</p>
                    <p><strong>Date/Time:</strong> ${message.date_time}</p>
                `;

                incidentContent.appendChild(messageDiv);
            });
        })
        .catch((error) => {
            console.error('Error fetching messages:', error);
            alert('Failed to load messages. Please try again.');
        });
}


function viewStatistics() {
    const statisticsContainer = document.getElementById('statistics-container');
    statisticsContainer.style.display = 'block'; 

    fetch('GetStatistics', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error('Failed to fetch statistics');
            }
            return response.json();
        })
        .then((data) => {
            
            drawIncidentChart(data.incidents);
            drawUserVolunteerChart(data.user_volunteers);
            drawParticipantChart(data.participants);
        })
        .catch((error) => {
            console.error('Error fetching statistics:', error);
            alert('Failed to load statistics. Please try again.');
        });
}


function drawIncidentChart(incidents) {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Incident Type');
    data.addColumn('number', 'Count');
    incidents.forEach((incident) => {
        data.addRow([incident.type, incident.count]);
    });

    const options = {
        title: 'Number of Incidents by Type',
    };

    const chart = new google.visualization.PieChart(document.getElementById('incident-chart'));
    chart.draw(data, options);
}


function drawUserVolunteerChart(userVolunteers) {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Category');
    data.addColumn('number', 'Count');
    userVolunteers.forEach((item) => {
        data.addRow([item.category, item.count]);
    });

    const options = {
        title: 'Number of Users and Volunteers',
    };

    const chart = new google.visualization.PieChart(document.getElementById('user-volunteer-chart'));
    chart.draw(data, options);
}


function drawParticipantChart(participants) {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Volunteer Type');
    data.addColumn('number', 'Count');
    participants.forEach((participant) => {
        data.addRow([participant.type, participant.count]);
    });

    const options = {
        title: 'Number of Participants (Vehicles and Firemen)',
        legend: { position: 'none' },
    };

    const chart = new google.visualization.BarChart(document.getElementById('participant-chart'));
    chart.draw(data, options);
}

function createParticipantSeats() {
    const incidentId = sessionStorage.getItem('selectedIncidentID');
    const seatCount = document.getElementById('seatCount').value;
    const volunteerType = document.getElementById('volunteer_type').value;

    const requestData = {
        incident_id: incidentId,
        seatCount: seatCount,
        volunteer_type: volunteerType
    };

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'CreateSeats', true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                alert("Seats created successfully!");
                document.getElementById('addSeatsForm').reset();
            } else {
                alert("Failed to create seats. Please try again.");
                console.error(xhr.responseText);
            }
        }
    };

    xhr.onerror = function () {
        alert("Network error. Please try again.");
    };

    xhr.send(JSON.stringify(requestData));
}


function showAddIncidentForm() {
    const formContainer = document.getElementById("incidentFormContainer");
    formContainer.style.display = "block"; 
}

function viewUserMessages() {
    const userContent = document.getElementById('userContent');
    const placeholder = document.getElementById('placeholder');

    
    userContent.innerHTML = '<p>Loading messages...</p>';
    if (placeholder) placeholder.style.display = 'none';
    
    fetch('GetMessagesUser', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error('Failed to load messages');
            }
            return response.json();
        })
        .then((messages) => {
            console.log("Raw Messages:", messages); 

            userContent.innerHTML = ''; 

            if (messages.length === 0) {
                userContent.innerHTML = '<p>No messages found.</p>';
                return;
            }

            
            const publicMessages = messages.filter((message) => {
                return message.recipient.toLowerCase() === 'public';
            });

            if (publicMessages.length === 0) {
                userContent.innerHTML = '<p>No public messages available.</p>';
                return;
            }

            
            publicMessages.forEach((message) => {
                const messageDiv = document.createElement('div');
                messageDiv.classList.add('message-item');

                messageDiv.innerHTML = `
                    <p><strong>Message ID:</strong> ${message.message_id}</p>
                    <p><strong>Incident ID:</strong> ${message.incident_id}</p>
                    <p><strong>Message:</strong> ${message.message}</p>
                    <p><strong>Sender:</strong> ${message.sender}</p>
                    <p><strong>Recipient:</strong> ${message.recipient}</p>
                    <p><strong>Date/Time:</strong> ${message.date_time}</p>
                `;

                userContent.appendChild(messageDiv);
            });
        })
        .catch((error) => {
            console.error('Error fetching messages:', error);
            alert('Failed to load messages. Please try again.');
        });
}

function sendUserMessage() {
    const incidentId = document.getElementById('message_incident_id').value;
    const message = document.getElementById('user_message').value.trim();
    const recipient = document.getElementById('recipient').value;
     const sender = sessionStorage.getItem('globalUsername');

    if (!message) {
        alert("Message cannot be empty.");
        return;
    }

    const messageData = {
        incident_id: incidentId,
        message: message,
        recipient: recipient,
        sender: sender
    };

    fetch('SendUserMessage', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(messageData)
    })
        .then(response => {
            if (response.ok) {
                alert("Message sent successfully!");
                document.getElementById('userMessageForm').reset(); 
                document.getElementById('userMessageFormContainer').style.display = 'none'; 
            } else {
                throw new Error("Failed to send the message.");
            }
        })
        .catch(error => {
            console.error("Error sending message:", error);
            alert("An error occurred. Please try again.");
        });
}

function showNearbyIncidents() {
    
    fetch("GetUserLocation", {
        method: "GET",
        headers: { "Content-Type": "application/json" }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch user's location.");
            }
            return response.json();
        })
        .then(userLocation => {
            const userLat = userLocation.lat;
            const userLon = userLocation.lon;

            if (!userLat || !userLon) {
                alert("User's location is not available.");
                return;
            }

            
            return fetch("LoadIncidentsUser", {
                method: "GET",
                headers: { "Content-Type": "application/json" }
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error("Failed to fetch incidents.");
                    }
                    return response.json();
                })
                .then(incidents => {
                    if (!incidents || incidents.length === 0) {
                        alert("No incidents available.");
                        return;
                    }

                    // TrueWay Matrix API call !!!
                    const origins = `${userLat},${userLon}`;
                    const destinations = incidents
                        .map(incident => `${incident.lat},${incident.lon}`)
                        .join(";");

                    const matrixApiUrl = `https://trueway-matrix.p.rapidapi.com/CalculateDrivingMatrix?origins=${origins}&destinations=${destinations}`;

                    return fetch(matrixApiUrl, {
                        method: "GET",
                        headers: {
                            "x-rapidapi-key": "f3cbf2db72mshdc22730fbbe08b9p169dfajsna303473fae5c",
                            "x-rapidapi-host": "trueway-matrix.p.rapidapi.com"
                        }
                    })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error("Failed to fetch distance data.");
                            }
                            return response.json();
                        })
                        .then(distanceData => {
                            if (!distanceData || !distanceData.distances) {
                                alert("Could not fetch distances.");
                                return;
                            }

                            //filter incidents within 30km
                            const nearbyIncidents = incidents.filter((incident, index) => {
                                const distance = distanceData.distances[0][index];
                                return distance <= 30000;
                            });

                            if (nearbyIncidents.length === 0) {
                                alert("No nearby incidents found.");
                                document.getElementById("Map").style.display = "none";
                                return;
                            }

                           
                            document.getElementById("Map").style.display = "block";
                            clearMarkers();
                            nearbyIncidents.forEach(incident => {
                                addMarker(
                                    parseFloat(incident.lat),
                                    parseFloat(incident.lon),
                                    `Incident ID: ${incident.incident_id}<br>Description: ${incident.description}`
                                );
                            });

                            
                            centerMap(userLat, userLon);
                        });
                });
        })
        .catch(error => {
            console.error("Error:", error);
            alert(error.message || "An error occurred.");
        });
}


////////////////////////VOLUNTEEEEERS////////////////////////////////////////////


function loadVolunteer() {
    const xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.status === 200) {
            try {
                const parsedResponse = JSON.parse(xhr.responseText);
                console.log("Parsed response:", parsedResponse);

                if (parsedResponse.length > 0) {
                    const user = parsedResponse[0];
                    const tableContent = createVolunteerCardJSON(user);
                    document.getElementById("volunteerContent").innerHTML = tableContent;
                    populateForm(user);
                } else {
                    document.getElementById("volunteerContent").innerHTML = "<p>No volunteer data available.</p>";
                }
            } catch (error) {
                console.error("JSON parsing error:", error);
                document.getElementById("volunteerContent").innerHTML = "<p>Invalid JSON response.</p>";
            }
        } else {
            console.error("Request failed with status:", xhr.status);
            document.getElementById("volunteerContent").innerHTML =
                "Request failed. Returned status of " + xhr.status;
        }
    };
    xhr.onerror = function () {
        alert("Network Error. Please try again.");
    };
    xhr.open("GET", "LoadVolunteer");
    xhr.send();
}


function updateVolunteerInfo() {
    var formData = new FormData(document.getElementById('volunteerForm'));
    var updatedData = {};

    
    formData.forEach((value, key) => {
        if (value.trim() !== "") {
            updatedData[key] = value;
        }
    });
    
    
    const lat = parseFloat(document.getElementById('volunteer_lat').value.trim()) || 0.0;
    const lon = parseFloat(document.getElementById('volunteer_lon').value.trim()) || 0.0;

    updatedData.lat = lat;
    updatedData.lon = lon;
    
    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'UpdateVolunteer'); 
    xhr.setRequestHeader('Content-type', 'application/json');
    xhr.onload = function () {
        if (xhr.status === 200) {
            alert("Your information has been updated successfully!");
        } else {
            alert("Failed to update your information. Please try again.");
        }
    };
    xhr.onerror = function () {
        alert("Network error. Please try again.");
    };

    xhr.send(JSON.stringify(updatedData));
}


function loadIncidentsVolunteer() {
    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log("Response data:", xhr.responseText);
            try {
                const parsedResponse = JSON.parse(xhr.responseText);
                console.log("Parsed response:", parsedResponse);
                let tableContent = loadIncidentTableVolunteerJSON(parsedResponse); //!!!!!!!
                document.getElementById('volunteerContent').innerHTML = tableContent;
            } catch (error) {
                console.error("Error parsing JSON:", error);
                document.getElementById('volunteerContent').innerHTML = '<p>Invalid JSON response.</p>';
            }
        } else {
            console.error("Request failed with status:", xhr.status);
            document.getElementById('volunteerContent').innerHTML = 'Request failed. Returned status of ' + xhr.status;
        }
    };
    xhr.onerror = function () { 
        alert("Network Error. Please try again."); 
    };
    xhr.open('GET', 'LoadIncidentsUser'); 
    xhr.send();
}

function selectIncidentVolunteer(incidentId) {
    console.log(`Incident ID: ${incidentId}`);
    sessionStorage.setItem('selectedIncidentID', incidentId); 

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'GetIncidentDetails', true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    const requestData = JSON.stringify({
        incident_id: incidentId
    });

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const response = JSON.parse(xhr.responseText);
                console.log('Response from servlet:', response);

                
                const messageIncidentIdField = document.getElementById('volunteer_message_incident_id');
                const messageFormContainer = document.getElementById('volunteerMessageFormContainer');

                if (messageIncidentIdField && messageFormContainer) {
                    messageIncidentIdField.value = response.incident_id || "";
                    messageFormContainer.style.display = 'block'; 
                } else {
                    console.error("Volunteer message form elements not found in the DOM.");
                }
            } else {
                console.error('Error:', xhr.responseText);
            }
        }
    };

    xhr.send(requestData);
}

function sendVolunteerMessage() {
    const incidentId = document.getElementById('volunteer_message_incident_id').value;
    const message = document.getElementById('volunteer_message').value.trim();
    const recipient = document.getElementById('recipient').value;
    const sender = sessionStorage.getItem('globalUsername');
    const volunteerUsername = document.getElementById('volunteer_username').value.trim();

    if (!message) {
        alert("Message cannot be empty.");
        return;
    }
    

    const messageData = {
        incident_id: incidentId,
        message: message,
        recipient: recipient,
        sender: sender
    };
    
        //add volunteer username if recipient is "volunteer"
    if (recipient === "volunteer") {
        if (!volunteerUsername) {
            alert("Please enter a volunteer username.");
            return;
        }
        messageData.recipient = volunteerUsername;
    }

    fetch('SendVolunteerMessage', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(messageData)
    })
        .then(response => {
            if (response.ok) {
                alert("Message sent successfully!");
                document.getElementById('volunteerMessageForm').reset(); 
                document.getElementById('volunteerMessageFormContainer').style.display = 'none'; 
            } else {
                throw new Error("Failed to send the message.");
            }
        })
        .catch(error => {
            console.error("Error sending message:", error);
            alert("An error occurred. Please try again.");
        });
}

function toggleVolunteerField() {
    const recipient = document.getElementById("recipient").value;
    const volunteerField = document.getElementById("volunteerUsernameField");

    if (recipient === "volunteer") {
        volunteerField.style.display = "block"; 
    } else {
        volunteerField.style.display = "none"; 
        document.getElementById("volunteer_username").value = ""; 
    }
}

function loadVolunteerMessages() {
    fetch("GetMessagesVolunteer", {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch messages.");
            }
            return response.json();
        })
        .then(messages => {
            if (messages.length === 0) {
                document.getElementById("volunteerMessages").innerHTML = "<p>No messages available.</p>";
                return;
            }

            // Generate the message list
            let messageList = `<ul class="message-list">`;
            messages.forEach(msg => {
                messageList += `
                    <li class="message-item">
                        <p><strong>Incident ID:</strong> ${msg.incident_id}</p>
                        <p><strong>From:</strong> ${msg.sender}</p>
                        <p><strong>To:</strong> ${msg.recipient}</p>
                        <p><strong>Message:</strong> ${msg.message}</p>
                        <p><strong>Date:</strong> ${new Date(msg.date_time).toLocaleString()}</p>
                    </li>
                `;
            });
            messageList += `</ul>`;

            document.getElementById("volunteerMessages").innerHTML = messageList;
        })
        .catch(error => {
            console.error(error);
            document.getElementById("volunteerMessages").innerHTML = "<p>An error occurred while loading messages.</p>";
        });
}

function loadVolunteerHistory() {
    fetch('GetVolunteerHistory', {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch volunteer history.');
            }
            return response.json();
        })
        .then(history => {
            const container = document.getElementById('volunteerContent');
            if (history.length === 0) {
                container.innerHTML = "<p>No participation history available.</p>";
                return;
            }

            let tableContent = `
                <table class="history-table">
                    <thead>
                        <tr>
                            <th>Incident ID</th>
                            <th>Type</th>
                            <th>Description</th>
                            <th>Start Date</th>
                            <th>End Date</th>
                            <th>Danger</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
            `;
            history.forEach(incident => {
                tableContent += `
                    <tr>
                        <td>${incident.incident_id}</td>
                        <td>${incident.incident_type}</td>
                        <td>${incident.description}</td>
                        <td>${incident.start_datetime || 'N/A'}</td>
                        <td>${incident.end_datetime || 'N/A'}</td>
                        <td>${incident.danger}</td>
                        <td>${incident.status}</td>
                    </tr>
                `;
            });
            tableContent += `
                    </tbody>
                </table>
            `;
            container.innerHTML = tableContent;
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('volunteerContent').innerHTML = '<p>An error occurred while loading history.</p>';
        });
}


function loadActiveIncidents() {
    const volunteerType = sessionStorage.getItem("volunteerType");

    fetch(`GetActiveIncidentsForVolunteers?volunteer_type=${volunteerType}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch incidents.");
            }
            return response.json();
        })
        .then(incidents => {
            if (incidents.length === 0) {
                document.getElementById("incidentList").innerHTML = `<p class="no-incidents">No active incidents available.</p>`;
                return;
            }

            let incidentCards = `<div class="incident-cards">`;
            incidents.forEach(incident => {
                incidentCards += `
                    <div class="incident-card">
                        <h3>Incident #${incident.incident_id}</h3>
                        <p><strong>Type:</strong> ${incident.incident_type}</p>
                        <p><strong>Description:</strong> ${incident.description}</p>
                        <p><strong>Danger Level:</strong> ${incident.danger}</p>
                        <button class="accept-btn" onclick="acceptIncident(${incident.incident_id})">Accept</button>
                    </div>`;
            });
            incidentCards += `</div>`;
            document.getElementById("incidentList").innerHTML = incidentCards;
        })
        .catch(error => {
            console.error(error);
            alert("An error occurred while loading incidents.");
        });
}

    

function acceptIncident(incidentId) {
    fetch("AcceptIncidentParticipation", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `incident_id=${incidentId}`
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to accept incident.");
            }
            return response.json();
        })
        .then(result => {
            if (result.success) {
                alert("You have successfully accepted the incident.");
                loadActiveIncidents();
            } else {
                alert(result.message || "Failed to accept the incident.");
            }
        })
        .catch(error => {
            console.error(error);
            alert("An error occurred while accepting the incident.");
        });
}




//function to handle API errors
function handleAPIError(xhr) {
    document.getElementById('msg').innerHTML = 'Request failed. Returned status of ' + xhr.status + "<br>" +
        JSON.stringify(xhr.responseText);
}


//JSON TABLES
function createUserTableJSON(data) {
    if (!data) {
        return `<p>No user data provided.</p>`;
    }

    // Generate a table for the specific user
    let tableContent = `
        <table border="1">
            <thead>
                <tr>
                    <th>User ID</th>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Password</th>
                    <th>Firstname</th>
                    <th>Lastname</th>
                    <th>Birthdate</th>
                    <th>Gender</th>
                    <th>Afm</th>
                    <th>Country</th>
                    <th>Address</th>
                    <th>Municipality</th>
                    <th>Prefecture</th>
                    <th>Job</th>
                    <th>Telephone</th>
                    <th>Lat</th>
                    <th>Lon</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>${data.user_id || 'N/A'}</td>
                    <td>${data.username || 'N/A'}</td>
                    <td>${data.email || 'N/A'}</td>
                    <td>${data.password || 'N/A'}</td>
                    <td>${data.firstname || 'N/A'}</td>
                    <td>${data.lastname || 'N/A'}</td>
                    <td>${data.birthdate || 0}</td>
                    <td>${data.gender || 'N/A'}</td>
                    <td>${data.afm || 'N/A'}</td>
                    <td>${data.country || 'N/A'}</td>
                    <td>${data.address || 'N/A'}</td>
                    <td>${data.municipality || 'N/A'}</td>
                    <td>${data.prefecture || 'N/A'}</td>
                    <td>${data.job || 'N/A'}</td>
                    <td>${data.telephone || 'N/A'}</td>
                    <td>${data.lat || 'N/A'}</td>
                    <td>${data.lon || 'N/A'}</td>
                </tr>
            </tbody>
        </table>
    `;
    return tableContent;
}

function createVolunteerCardJSON(data) {
    if (!data) {
        return `<p>No volunteer data provided.</p>`;
    }

    // Generate a card for the specific volunteer
    return `
        <div class="volunteer-card">
            <h2>Volunteer Details</h2>
            <p><strong>ID:</strong> ${data.volunteer_id || "N/A"}</p>
            <p><strong>Username:</strong> ${data.username || "N/A"}</p>
            <p><strong>Email:</strong> ${data.email || "N/A"}</p>
            <p><strong>Firstname:</strong> ${data.firstname || "N/A"}</p>
            <p><strong>Lastname:</strong> ${data.lastname || "N/A"}</p>
            <p><strong>Birthdate:</strong> ${data.birthdate || "N/A"}</p>
            <p><strong>Gender:</strong> ${data.gender || "N/A"}</p>
            <p><strong>AFM:</strong> ${data.afm || "N/A"}</p>
            <p><strong>Country:</strong> ${data.country || "N/A"}</p>
            <p><strong>Address:</strong> ${data.address || "N/A"}</p>
            <p><strong>Municipality:</strong> ${data.municipality || "N/A"}</p>
            <p><strong>Prefecture:</strong> ${data.prefecture || "N/A"}</p>
            <p><strong>Job:</strong> ${data.job || "N/A"}</p>
            <p><strong>Telephone:</strong> ${data.telephone || "N/A"}</p>
            <p><strong>Latitude:</strong> ${data.lat || "N/A"}</p>
            <p><strong>Longitude:</strong> ${data.lon || "N/A"}</p>
            <p><strong>Type:</strong> ${data.volunteer_type || "N/A"}</p>
            <p><strong>Height:</strong> ${data.height || "N/A"}</p>
            <p><strong>Weight:</strong> ${data.weight || "N/A"}</p>
        </div>
    `;
}





function createEditableTableFromJSON(data) {
    var html = "<table id='editableTable'><tr><th>Category</th><th>Value</th></tr>";
    for (const x in data) {
        var category = x;
        var value = data[x];

        // Fields that cannot be edited
        if (category === 'username' || category === 'telephone' || category === 'afm') {
            html += `<tr><td>${category}</td><td>${value}</td></tr>`;
        } else {
            html += `<tr><td>${category}</td><td contenteditable='true'>${value}</td></tr>`;
        }
    }
    html += "</table>";
    html += "<button class='button' onclick='submitChanges()'>Submit Changes</button>";
    return html;
}

function loadIncidentTableJSON(data) {
    let tableContent = `
    <table border="1">
        <thead>
            <tr>
                <th>Select</th>
                <th>Incident ID</th>
                <th>Type</th>
                <th>Description</th>
                <th>User Phone</th>
                <th>User Type</th>
                <th>Address</th>
                <th>Latitude</th>
                <th>Longitude</th>
                <th>Municipality</th>
                <th>Prefecture</th>
                <th>Start DateTime</th>
                <th>End DateTime</th>
                <th>Danger</th>
                <th>Status</th>
                <th>Final Result</th>
                <th>Vehicles</th>
                <th>Firemen</th>
            </tr>
        </thead>
        <tbody>
    `;
    data.forEach(incident => {
        tableContent += `
            <tr>
                <td><button class="select-button" onclick="selectIncident('${incident.incident_id}')">Select</button></td>
                <td>${incident.incident_id || 'N/A'}</td>
                <td>${incident.incident_type || 'N/A'}</td>
                <td>${incident.description || 'N/A'}</td>
                <td>${incident.user_phone || 'N/A'}</td>
                <td>${incident.user_type || 'N/A'}</td>
                <td>${incident.address || 'N/A'}</td>
                <td>${incident.lat || 'N/A'}</td>
                <td>${incident.lon || 'N/A'}</td>
                <td>${incident.municipality || 'N/A'}</td>
                <td>${incident.prefecture || 'N/A'}</td>
                <td>${incident.start_datetime || 'N/A'}</td>
                <td>${incident.end_datetime || 'N/A'}</td>
                <td>${incident.danger || 'N/A'}</td>
                <td>${incident.status || 'N/A'}</td>
                <td>${incident.finalResult || 'N/A'}</td>
                <td>${incident.vehicles || 'N/A'}</td>
                <td>${incident.firemen || 'N/A'}</td>
            </tr>
        `;
    });
    tableContent += `
        </tbody>
    </table>
    `;
    return tableContent;
}



function loadIncidentTableUserJSON(data) {
    let tableContent = `
    <table border="1">
        <thead>
            <tr>
                <th>Select</th>
                <th>Incident ID</th>
                <th>Type</th>
                <th>Description</th>
                <th>User Phone</th>
                <th>User Type</th>
                <th>Address</th>
                <th>Latitude</th>
                <th>Longitude</th>
                <th>Municipality</th>
                <th>Prefecture</th>
                <th>Start DateTime</th>
                <th>End DateTime</th>
                <th>Danger</th>
                <th>Status</th>
                <th>Final Result</th>
                <th>Vehicles</th>
                <th>Firemen</th>
            </tr>
        </thead>
        <tbody>
    `;
    data.forEach(incident => {
        tableContent += `
            <tr>
                <td><button class="select-button" onclick="selectIncidentUser('${incident.incident_id}')">Select</button></td>
                <td>${incident.incident_id || 'N/A'}</td>
                <td>${incident.incident_type || 'N/A'}</td>
                <td>${incident.description || 'N/A'}</td>
                <td>${incident.user_phone || 'N/A'}</td>
                <td>${incident.user_type || 'N/A'}</td>
                <td>${incident.address || 'N/A'}</td>
                <td>${incident.lat || 'N/A'}</td>
                <td>${incident.lon || 'N/A'}</td>
                <td>${incident.municipality || 'N/A'}</td>
                <td>${incident.prefecture || 'N/A'}</td>
                <td>${incident.start_datetime || 'N/A'}</td>
                <td>${incident.end_datetime || 'N/A'}</td>
                <td>${incident.danger || 'N/A'}</td>
                <td>${incident.status || 'N/A'}</td>
                <td>${incident.finalResult || 'N/A'}</td>
                <td>${incident.vehicles || 'N/A'}</td>
                <td>${incident.firemen || 'N/A'}</td>
            </tr>
        `;
    });
    tableContent += `
        </tbody>
    </table>
    `;
    return tableContent;
}

function loadIncidentTableVolunteerJSON(data) {
    let tableContent = `
    <table border="1">
        <thead>
            <tr>
                <th>Select</th>
                <th>Incident ID</th>
                <th>Type</th>
                <th>Description</th>
                <th>User Phone</th>
                <th>User Type</th>
                <th>Address</th>
                <th>Latitude</th>
                <th>Longitude</th>
                <th>Municipality</th>
                <th>Prefecture</th>
                <th>Start DateTime</th>
                <th>End DateTime</th>
                <th>Danger</th>
                <th>Status</th>
                <th>Final Result</th>
                <th>Vehicles</th>
                <th>Firemen</th>
            </tr>
        </thead>
        <tbody>
    `;
    data.forEach(incident => {
        tableContent += `
            <tr>
                <td><button class="select-button" onclick="selectIncidentVolunteer('${incident.incident_id}')">Select</button></td>
                <td>${incident.incident_id || 'N/A'}</td>
                <td>${incident.incident_type || 'N/A'}</td>
                <td>${incident.description || 'N/A'}</td>
                <td>${incident.user_phone || 'N/A'}</td>
                <td>${incident.user_type || 'N/A'}</td>
                <td>${incident.address || 'N/A'}</td>
                <td>${incident.lat || 'N/A'}</td>
                <td>${incident.lon || 'N/A'}</td>
                <td>${incident.municipality || 'N/A'}</td>
                <td>${incident.prefecture || 'N/A'}</td>
                <td>${incident.start_datetime || 'N/A'}</td>
                <td>${incident.end_datetime || 'N/A'}</td>
                <td>${incident.danger || 'N/A'}</td>
                <td>${incident.status || 'N/A'}</td>
                <td>${incident.finalResult || 'N/A'}</td>
                <td>${incident.vehicles || 'N/A'}</td>
                <td>${incident.firemen || 'N/A'}</td>
            </tr>
        `;
    });
    tableContent += `
        </tbody>
    </table>
    `;
    return tableContent;
}

function loadIncidentTableGuestJSON(data) {
    let tableContent = `
    <table class="incident-table">
        <thead>
            <tr>
                <th>Incident ID</th>
                <th>Type</th>
                <th>Description</th>
                <th>User Phone</th>
                <th>User Type</th>
                <th>Address</th>
                <th>Latitude</th>
                <th>Longitude</th>
                <th>Municipality</th>
                <th>Prefecture</th>
                <th>Start DateTime</th>
                <th>End DateTime</th>
                <th>Danger</th>
                <th>Status</th>
                <th>Final Result</th>
                <th>Vehicles</th>
                <th>Firemen</th>
            </tr>
        </thead>
        <tbody>
    `;
    data.forEach(incident => {
        tableContent += `
            <tr>
                <td>${incident.incident_id || 'N/A'}</td>
                <td>${incident.incident_type || 'N/A'}</td>
                <td>${incident.description || 'N/A'}</td>
                <td>${incident.user_phone || 'N/A'}</td>
                <td>${incident.user_type || 'N/A'}</td>
                <td>${incident.address || 'N/A'}</td>
                <td>${incident.lat || 'N/A'}</td>
                <td>${incident.lon || 'N/A'}</td>
                <td>${incident.municipality || 'N/A'}</td>
                <td>${incident.prefecture || 'N/A'}</td>
                <td>${incident.start_datetime || 'N/A'}</td>
                <td>${incident.end_datetime || 'N/A'}</td>
                <td>${incident.danger || 'N/A'}</td>
                <td>${incident.status || 'N/A'}</td>
                <td>${incident.finalResult || 'N/A'}</td>
                <td>${incident.vehicles || 'N/A'}</td>
                <td>${incident.firemen || 'N/A'}</td>
            </tr>
        `;
    });
    tableContent += `
        </tbody>
    </table>
    `;
    return tableContent;
}


//toggle logic
document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("simple_user").onclick = function () {
        toggleVolunteerFields(false);
    };

    document.getElementById("volunteer_firefighter").onclick = function () {
        toggleVolunteerFields(true);
    };
});


//if it is a simple user dont show any special fields

document.addEventListener("DOMContentLoaded", function() {
    document.getElementById("simple_user").onclick = function() {
        const volunteerFields = document.getElementById("volunteer-fields");
        const termsLabel = document.getElementById("terms-label");

        volunteerFields.style.display = "none";
        termsLabel.textContent = "Συμφωνώ με τους όρους χρήσης:";
    };
});
    
