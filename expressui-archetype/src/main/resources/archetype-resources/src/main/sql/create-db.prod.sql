-- Design to be executed against MySQL, production database

CREATE DATABASE ${artifactId};
GRANT ALL PRIVILEGES ON ${artifactId}.* TO ${artifactId}@localhost IDENTIFIED BY '';
