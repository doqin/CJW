let mouseX;
let mouseY;
const catSpeed = 2;
const fps = 60;
const interval = 1000 / fps;
document.addEventListener('mousemove', function(event) {
   // console.log('Mouse X:', event.clientX, 'Mouse Y:', event.clientX);
   mouseX = event.clientX;
   mouseY = event.clientY;
});

function catChase() {
   let cat = document.querySelector("#cat");
   let catX = parseFloat(cat.style.left.replace("px", "")) + cat.clientWidth / 2;
   let catY = parseFloat(cat.style.top.replace("px", "")) + cat.clientHeight / 2;
   let deltaX = mouseX - catX;
   let deltaY = mouseY - catY;

   if (Math.abs(deltaX) < cat.clientWidth / 2 && Math.abs(deltaY) < cat.clientHeight / 2) {
      console.log('cat gotchu', "died");
      window.location.reload();
   }

   let length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
   // Normalize
   if (length > 0) {
      deltaX = deltaX / length;
      deltaY = deltaY / length;
   }
   // console.log('delta X:', deltaX, 'delta Y:', deltaY);
   cat.style.left = catX - cat.clientWidth / 2 + deltaX * catSpeed + "px";
   cat.style.top = catY - cat.clientHeight / 2 + deltaY * catSpeed + "px";
}

setInterval(catChase, interval);