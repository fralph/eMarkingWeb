// Library that we don't know why is here
var moment = require('moment');

// NodeJS configuration, this should be read from somewhere else probably
var port=9091;
var ipaddress="127.0.0.1";

console.log(logtime() + "Starting NodeJS for e-marking");

var app = require('http').createServer(handler);
app.listen(port,ipaddress);
var  io = require('socket.io').listen(app);
var  fs = require('fs');
var _ = require('underscore')._;

console.log(logtime() + "NodeJS for e-marking started on " + ipaddress + ":" + port);

// This function is used in createServer for http. Check if we can skip it
function handler (req, res) {
	fs.readFile('index.html',

			function (err, data) {
		if (err) {
			res.writeHead(500);
			return res.end('Error loading index.html');
		}
		res.setHeader("Content-Type", "text/html");
		res.writeHead(200);
		res.end(data);
	});
}

// This is the real stuff. sockets.io on connection
// with several events. This happens when a new socket
// has connected. In which case we add event listeners
// which are: joinserver, disconnect and sendmessage
io.sockets.on("connection", function (socket) {
	
	// When the new socket has just joined the server. Called from
	// the function LoadNodeJs in GWT
	socket.on("joinserver", function(data) {
		
		// We parse the data sent from GWT
		var conectionData=JSON.parse(data);
		
		// First, fill the socket with data from the interface
		socket.room = conectionData.cm;
		socket.first=conectionData.first;
		socket.last=conectionData.last;
		socket.email=conectionData.email;
		socket.userid=conectionData.userid;

		// Join the room with the socket (so we can broadcast)
		socket.join(socket.room);

		// Get the list of all sockets currently in the room
		var users = [];
		var clients_in_the_room = io.sockets.adapter.rooms[socket.room];
		
		// Make a users list
		for (var clientId in clients_in_the_room ) {
			var client_socket = io.sockets.connected[clientId];
			
			// We create a user object to push in the list
			var user = {};
			user.first=client_socket.first;
			user.last=client_socket.last;
			user.email=client_socket.email;
			user.userid=client_socket.userid;
			
			users.push(user);
		}
		
		// Broadcast to room (this one makes sense)
		io.to(socket.room).emit("onJoinServer", JSON.stringify(users));
		
		// Log the new user
		console.log(logtime() + socket.first + " " + socket.last + " has entered room " + socket.room);
	});
	
	// When a message is sent from a user
	socket.on("sendmessage", function(data) {
		var data= JSON.parse(data);
		
		var message=data.message;
		var source =data.source;

		var obj={};
		obj.time=unixtime();
		obj.message=message;
		obj.userid=socket.userid;
		obj.source=source;

		// Broadcast to the room
		io.to(socket.room).emit("onSendMessage", JSON.stringify(obj));//solo envia un mensaje
		
		console.log(logtime() + socket.first + " " + socket.last + " in room " + socket.room + " has sent message: " + message);


	});

	// Disconnecting from the server
	socket.on("disconnect", function() {
		// Built in room management (perfect)
		socket.leave(socket.room);

		var user = {};
		user.first=socket.first;
		user.last=socket.last;
		user.email=socket.email;
		user.userid=socket.userid;

		// We tell all others in the room that we are not connected anymore
		socket.broadcast.to(socket.room).emit("onDisconnect", JSON.stringify(user));
		
		console.log(logtime() + socket.first + " " + socket.last + " has left room " + socket.room);
	});
});

function logtime(){
	var lognow = moment(new Date());
	var logtime = lognow.format("YYYY-MM-DD HH:mm:ss");
	return "["+logtime+"] ";

}

function unixtime(){
	var now = moment(new Date());
	var time = now.format("X");
	return time;

}
