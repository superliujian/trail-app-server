#
# Creates the Trail database
# Run with mysql -u [user] -p -vvv < schema.sql

# DROP DATABASE IF EXISTS trail; # Uncomment to replace existing databases (dangerous)
CREATE DATABASE trail;
USE trail;

CREATE TABLE comment ( 
    comment_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    lat DECIMAL(10, 8) NOT NULL, # Decimals to prevent precision loss
    lng DECIMAL(11, 8) NOT NULL,
    title VARCHAR(100),
    body VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attachment_id INT UNSIGNED,
    PRIMARY KEY(comment_id)
    FOREIGN KEY(attachment)
);

CREATE TABLE attachment {
	attachment_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	PRIMARY KEY(attachment_id)
}