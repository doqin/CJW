async function getlink() {
    const res = await fetch('/api/val-fetcher');

    const data = await res.json();
    // console.log(data);

    // if (data.response_code_1 !== "200") {
    //     document.getElementById('status').innerText = `Error fetching data 1\nError code: ${data.response_code_1}`;
    //     return;
    // }

    if (data.response_code_2 !== "200") {
        document.getElementById('status').innerText = `Error fetching data 2\nError code: ${data.response_code_2}`;
        return;
    } else {
        document.getElementById('status').innerText = "";
    }

    if (/*data.account_level === undefined || */ data.currenttierpatched === undefined || data.patched_tier === undefined) {
        document.getElementById('status').innerText = "Error fetching data";
        return;
    }
    let p = document.getElementById('player_info');
    p.innerText = `Player: ${data.name}#${data.tag}\n`;
    switch (data.region) {
        case "ap":
            p.innerText += "Region: Asia Pacific\n";
            break;
        case "na":
            p.innerText += "Region: North America\n";
            break;
        case "eu":
            p.innerText += "Region: Europe\n";
            break;
        case "kr":
            p.innerText += "Region: Korea\n";
            break;
        case "latam":
            p.innerText += "Region: Latin America\n";
            break;
        case "br":
            p.innerText += "Region: Brazil\n";
            break;
        default:
            p.innerText += `Region: ${data.region}\n`;
    }

    p = document.getElementById('current_rank');
    // p.innerText = `Account level: ${data.account_level}\n`;
    p.innerText = `Current rank: ${data.currenttierpatched}\n`;

    document.getElementById('current_img').src = data.currentimg_small;

    p = document.getElementById('peak_rank');
    p.innerText = `Peak rank: ${data.patched_tier}\n`;
}

document.addEventListener("DOMContentLoaded", () => {
    setInterval(getlink, 5000); // poll every 5s
})
