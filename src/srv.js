var http = require("http");
var url = require("url");
var mysql = require("mysql");
var fs = require("fs");
var op = require("./op");

var conn;
var details;
var srv;
var port = 8080;

function onRequest(request, response) {
  var u = url.parse(request, true);
  var handler = op.get(url.pathname.toLowerCase());
  if (handler) {
    // Serve a response if the path is valid
    response.writeHead(200, {"Content-Type" : "x-application/json"});
    response.end(handler(u.query));
  }
}

function onClose() {
  conn.destroy();
}

function start() {
  // Load connection properties
  fs.readFile("./connection.json", "utf8", function (err, data) {
    if (err) {
      throw err;
    }
    details = JSON.parse(data);
    console.log("Connecting database");

    // Connect to the database
    conn = mysql.createConnection(details);
    conn.connect(function (err) {
      if (err) {
        throw err;
      }
      srv.listen(port); // Start when ready
      console.log("Listening on port %d", port);
    });
  });

  // Create the HTTP server
  srv = http.createServer();
  srv.on("request", onRequest);
  srv.on("close", onClose);
}
exports.start = start;
