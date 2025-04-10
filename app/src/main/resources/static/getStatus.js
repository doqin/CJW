async function getStatus() {
    const res = await fetch('/api/currently-playing');
    if (res.status === 204) {
        document.getElementById('status').innerText = "Doqin is not listening to anything.";
        return;
    }
    if (res.status === 401) {
        document.getElementById('status').innerText = "Not logged in.";
        return;
    }

    const data = await res.json();
    if (data.track === undefined || data.artist === undefined) {
        document.getElementById('status').innerText = "Error fetching Doqin's music data";
        return;
    }
    document.getElementById('status').innerText = `Doqin is listening to: ${data.track} by ${data.artist}`;
}

document.addEventListener("DOMContentLoaded", () => {
    getStatus();
    setInterval(getStatus, 5000); // poll every 5s
})