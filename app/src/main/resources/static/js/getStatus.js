async function getlink() {
  const res = await fetch('/api/currently-playing');
  if (res.link === 204) {
    document.getElementById('status').innerText = "Doqin is not listening to anything.";
    return;
  }
  if (res.link === 401) {
    document.getElementById('status').innerText = "Not logged in.";
    return;
  }

  const data = await res.json();
  if (data.track === undefined || data.artist === undefined) {
    document.getElementById('status').innerText = "Error fetching Doqin's music data";
    return;
  }
  let a = document.createElement("a");
  a.href = `https://open.spotify.com/track/${data.link}`
  a.textContent = `${data.track}`;
  let p = document.getElementById('status');
  p.innerText = "Doqin is listening to: ";
  p.appendChild(a);
  p.appendChild(document.createTextNode(` by ${data.artist}`));
}

document.addEventListener("DOMContentLoaded", () => {
  setInterval(getlink, 5000); // poll every 5s
})
