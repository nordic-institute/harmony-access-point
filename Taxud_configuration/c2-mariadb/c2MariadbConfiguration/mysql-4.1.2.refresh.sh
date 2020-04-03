schema=$1
mysql_file=mysql5innoDb-4.1.2.ddl
mysql_data_file=mysql5innoDb-4.1.2-data.ddl
rootpwd=Taxud_C2Db
mysql -h localhost -u root --password=$rootpwd -e "drop schema if exists $schema;create schema $schema;alter database $schema charset=utf8 collate=utf8_bin;drop user if exists $schema@'%';create user $schema@'%' identified by '$schema';grant all on $schema.* to $schema@'%';"
mysql -h localhost -u root --password=$rootpwd $schema < $mysql_file
mysql -h localhost -u root --password=$rootpwd $schema < $mysql_data_file
