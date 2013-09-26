// Modules
var http = require("http");
var url = require("url");
var mysql = require("db-mysql");
var fs = require("fs");

// Locally bound variables
var db;
var port = 8080;

function start() {
	// Load connection properties
	var details;
	fs.readFile("./connection.json", "utf8", function(err, data) {
		if (err) throw err;
		details = JSON.parse(data);
	}

	// Create the HTTP server
	var srv = http.createServer();
	srv.on("request", onRequest);
	srv.on("close", onClose);

	// Connect to the database
	db = mysql.Database(details).connect(function(err) {
		if (err) throw err;
		srv.listen(port); // Start when ready
		console.log("Listening on port %d", port);
	});
}
exports.start = start;

function onRequest(request, response) {
	var url = url.parse(request, true);
	var op = ops[url.pathname.toLowerCase()];
	if (op) {
		// Serve a response if the path is valid
		response.writeHead(200, {"Content-Type" : "x-application/json"});
		response.end(op(url.query));
	}
}

function onClose() {
	db.disconnect();
}

// TODO Move to separate module
var ops = {};

function addComment(querystring) {
	console.log("Added comment");
}
ops.add_comment = addComment;