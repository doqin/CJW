async function getlink() {
    const res = await fetch('/api/val-fetcher');

    const data = await res.json();
    console.log(data);
    if (/*data.account_level === undefined || */ data.currenttierpatched === undefined || data.patched_tier === undefined) {
        document.getElementById('status').innerText = "Error fetching data";
        return;
    }
    let p = document.getElementById('status');
    // p.innerText = `Account level: ${data.account_level}\n`;
    p.innerText = `Current rank: ${data.currenttierpatched}\nPeak rank: ${data.patched_tier}`;
}

document.addEventListener("DOMContentLoaded", () => {
    setInterval(getlink, 5000); // poll every 5s
})
