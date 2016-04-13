/**
 * By Jiayi Cao
 * Homebuilt STM Demo
*/

var scanning = false;
var animation;
var curX = 0;
var curY = 0;


function drawImage(){
		var ctx = $("#scanDisplay")[0].getContext('2d');

		var img = new Image();
		img.onload = function(){




			$("#scanDisplay").attr("height",img.height);
  			$("#scanDisplay").attr("width",img.width);
  			//$("#scanDisplay").attr("src","images/ex1.png");
  			var line = 0;
  			animation = setInterval(function(){

  					ctx.drawImage(img,0,line,img.width,1,0,line,img.width,img.height);
  					line ++;
  					if(line >= img.height){ 
  						$("#startBtn").text("Start");
  						$("#startBtn").removeClass("btn-success");
						$("#startBtn").addClass("btn-primary");
						$("#status").text("Not Scanning");
						$("#info").text("Please do not move the sample until the tip moves out.");
  						clearInterval(animation);
  					}
  				},100);
  			

  		
  			$("#scanDisplay").css("margin-left",($("#scanDisplay").parent().width() - img.width)/2);



		}

		img.src = "images/"+Math.floor((Math.random() * 3) + 1)+".jpg";
}

function start(){
	$("#startBtn").click(function(){

		if($("#startBtn").text() == "Start"){
			$("#startBtn").text("Update");
			$("#startBtn").removeClass("btn-primary");
			$("#startBtn").addClass("btn-success");
			$("#status").text("Scanning ...");
			$("#info").text("Please do not touch the sample.");
			drawImage();
		}else{

		}
	})
}

function drawMinimap(){

	var canvas = $("#minimap")[0];
	var ctx = canvas.getContext('2d');
	ctx.clearRect(0, 0, canvas.width, canvas.height);
	ctx.beginPath();
	ctx.lineWidth="2";
	ctx.strokeStyle="red";

	ctx.rect(curX,curY,50,50);
	ctx.stroke();
}
function libraryTab(){
	$("#main").append("<div id='libraryTab' class='mainFrame'><div class='title bg-primary' width=100%><h3>Saved Images</h3></div><div class='row'></div></div>");
	var imgNumber = 3;
	var folder = "images/"
	for(var i =1;i<=imgNumber;i++){
		$(".row").append( "<img class='im img-rounded img-responsive col-xs-6 col-md-4' src='"+ folder + i +".jpg"+"'>" );
	}
}
function scanTab(){
	
	$("#main").append("<div id='scanTab' class='mainFrame'><div id='topPanel'><canvas id='scanDisplay'></canvas></div><button id='startBtn' type='button' class='btn btn-primary stmBtn2'>Start</button><button id='stopBtn' type='button' class='btn btn-danger stmBtn2'>Stop</button></div>");
	$("#scanTab").append("<div class='input-group stmBtn2'><span class='input-group-addon' id='basic-addon3'>Scan Length</span><input value='492.38' type='text' class='form-control textf' id='scanLength' aria-describedby='basic-addon'></div>");
	$("#scanTab").append("<div class='input-group stmBtn2'><span class='input-group-addon' id='basic-addon3'>Scan Size</span><input value='400' type='text' class='form-control textf' id='scanSize' aria-describedby='basic-addon'></div>");
	$("#scanTab").append("<div class='input-group stmBtn2'><span class='input-group-addon' id='basic-addon3'>Increment</span><input value='1.23' type='text' class='form-control textf' id='increment' aria-describedby='basic-addon'></div>");
	$("#scanTab").append("<div class='input-group stmBtn2'><span class='input-group-addon' id='basic-addon3'>Scan Angle</span><input value='45.0' type='text' class='form-control textf' id='scanAngle' aria-describedby='basic-addon'></div>");

	$("#scanTab").append("<div id='leftPanel'></div>");
	$("#scanTab").append("<div id='rightPanel'></div>");

	$("#rightPanel").append("<span class='label label-pill label-primary'>Status: </span><div id='status'>Not Scanning</div><br><span class='label label-pill label-info'>Message: </span><div id='info'>None</div>");
	$("#leftPanel").append("<div id='arrowKeys'></div>");
	$("#arrowKeys").append("<div id='up' class='glyphicon glyphicon-arrow-up arrow'></div><br>");
	$("#arrowKeys").append("<div id='left' class='glyphicon glyphicon-arrow-left arrow'></div>");
	$("#arrowKeys").append("<div id='right' class='glyphicon glyphicon-arrow-right arrow'></div><br>");
	$("#arrowKeys").append("<div id='down' class='glyphicon glyphicon-arrow-down arrow'></div>");
	$("#leftPanel").append("<canvas id='minimap' height='150' width='150'></canvas>");

	drawMinimap();
	start();

	$("#stopBtn").click(function(){

  						$("#startBtn").text("Start");
  						$("#startBtn").removeClass("btn-success");
						$("#startBtn").addClass("btn-primary");
						$("#status").text("Not Scanning");
						$("#info").text("Please do not move the sample until the tip moves out.");
  						clearInterval(animation);
	})

	$("#up").click(function(){

		console.log("b");
		curY = curY - 50;
		if(curY < 0) curY = 0;
		drawMinimap();
	})

		$("#down").click(function(){

		curY = curY + 50;
		if(curY > 100) curY = 100;
		drawMinimap();
	})

			$("#left").click(function(){

		curX = curX - 50;
		if(curX < 0) curX = 0;
		drawMinimap();
	})
				$("#right").click(function(){

		curX = curX + 50;
		if(curX > 100) curX = 100;
		drawMinimap();
	})

}

function startUp(){

	$("#main").empty();
	$("#main").append("<button type='button' id='backBtn' class='btn btn-default' aria-label='Left Align'><span class='glyphicon glyphicon-log-out' aria-hidden='true'></span></button>");
	$("#main").append("<div id='startup' class='mainFrame'><div id='startDisplay'>Scanning Tunneling Microscope Web Terminal</div><button id='scanBtn' type='button' class='btn btn-primary stmBtn'>Scan</button><button id='libBtn' type='button' class='btn btn-primary stmBtn'>Library</button></div>");
	
	$("#backBtn").click(function(){
		startUp();
	});
	$("#scanBtn").click(function(){

		$("#startup").hide();
		scanTab();

	});

	$("#libBtn").click(function(){
		$("#startup").hide();
		libraryTab();
	})

}

$( document ).ready(function(){


	startUp();

})

