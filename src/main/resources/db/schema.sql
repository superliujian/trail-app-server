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
    body VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(comment_id)
);