<?php
	
	function translateGroupNameToId($array, $name) {
				
		if (is_numeric($name)) {
			return $name;
		}
				
		foreach ($array as $element) {
			if ($element['name'] == $name) {
				return $element['id'];
			}
		}
		
		//Gruppe ist nicht bekannt
		return -1;
	}
	
	//Gruppenid in Name übersetzen
	function translateGroupIdToName($array, $id) {
		
		foreach ($array as $element) {
			if ($element['id'] == $id) {
				return $element['name'];
			}
		}
		
		//Gruppe ist nicht bekannt
		return "-1";
	}
	
	//Erzeugt einen Anhang für die SQL Query mit der nach Gruppenitems gesucht werden kann
	function builtGroupQueryByName($names) {
		
		$name_array = explode("_", $names);
		$group_array = getGroupArray();
		$query = "";
		
		$id = translateGroupNameToId($group_array,$name_array[0]);
		
		if ($id != -1) {
			
			$query = "groupId = " . $id . " ";
			
			for ($x = 1; $x < count($name_array); $x++) {
		
				$id = translateGroupNameToId($group_array,$name_array[$x]);
		
				if ($id != -1) {
					
					$query = $query . " OR groupId = " . $id;
					
				}
				else {
					error_log("Es wurde eine fehlerhafte GruppenID übergeben!");
				}				
			}

			$query = $query . ")";
		}
		else {
			error_log("Es wurde eine fehlerhafte GruppenID übergeben!");
		}		
		return $query;
	}
	
	function getGroupArray() {
		
		global $dbtable;
		global $clientdbVersion;
		global $pdo;
	
		//SQL Query zum Abfragen der Daten konstruieren
		$queryString = "SELECT * FROM `groupx`";
		
		//Ausgabe per JSON
		$statment=$pdo->prepare($queryString);
			
		//Die Benutzereingaben sicher in den Querystring einfügen
		//$statment->bindParam(':clientdbversion', $clientdbVersion, PDO::PARAM_INT);
		
		//Statment schließen
		$statment->closeCursor();
		
		//SQL Abfrage ausführen
		$statment->execute();	
		
		//DEBUG Ausgabe SQL Query
		//$statment->debugDumpParams();
		
		$results = array();
	
		while($res=$statment->fetch(PDO::FETCH_ASSOC)){
			
			$results[] = $res;
			//print($results[0]["name"] . "-");
			//print($res["name"] . "-");
	 
		}
		
		return $results;
	}
	
		
	function translateGroup($group) {
				
		//$group = explode("_",$group);
		$group_array = getGroupArray();
		
		$idarray = array();
		
		foreach($group as $element) {			
			$idarray[] = translateGroupNameToId($group_array,$element);
		}
				
		return $idarray;		
	}
	
	function log_db($message) {
		$dbFile = fopen("config/log_db.txt",'a');
		
		fwrite($dbFile,"Timestamp: ");
		fwrite($dbFile,date("Y-m-d H:i:s"));
		fwrite($dbFile,"; Message: ");
		fwrite($dbFile,$message);
		fwrite($dbFile,"\r\n");
		
		fclose($dbFile);
	}
	
	function getDBVersion() {
		$dbFile = fopen("db_Version.txt",'r');
		$dbversion = fgets($dbFile);
		fclose($dbFile);
		return $dbversion;
	}
	
	function getDBAccess() {
		
		global $db_server;
		global $db_name;
		global $db_user;
		global $db_password;
		
		//Datenbankzugangsdaten
		$dbFile = fopen(__DIR__ .  "/config/access.txt",'r');
		
		$db_server = fgets($dbFile);	
		$db_name = fgets($dbFile);	
		$db_user = fgets($dbFile);
		$db_password = fgets($dbFile);
		
		fclose($dbFile);
		 
		$db_server = trim(preg_replace('/\s+/', ' ', $db_server));
		$db_name = trim(preg_replace('/\s+/', ' ', $db_name));
		$db_user = trim(preg_replace('/\s+/', ' ', $db_user));
		$db_password = trim(preg_replace('/\s+/', ' ', $db_password));
	}
	
	function createDatabaseHandler() {
		//Datenbankzugangsdaten
		$dbFile = fopen(__DIR__ .  "/config/access.txt",'r');
		
		$db_server = fgets($dbFile);	
		$db_name = fgets($dbFile);	
		$db_user = fgets($dbFile);
		$db_password = fgets($dbFile);
		
		fclose($dbFile);
		 
		$db_server = trim(preg_replace('/\s+/', ' ', $db_server));
		$db_name = trim(preg_replace('/\s+/', ' ', $db_name));
		$db_user = trim(preg_replace('/\s+/', ' ', $db_user));
		$db_password = trim(preg_replace('/\s+/', ' ', $db_password));
		
		//Zugangsobjekt erzeugen
		$pdo = new PDO('mysql:host=' . $db_server.';dbname=' . $db_name, $db_user , $db_password);
		return $pdo;
	}

	function debugJSON() {
		switch (json_last_error()) {
			case JSON_ERROR_NONE:
				echo ' - No errors';
				break;
			case JSON_ERROR_DEPTH:
				echo ' - Maximum stack depth exceeded';
				break;
			case JSON_ERROR_STATE_MISMATCH:
				echo ' - Underflow or the modes mismatch';
				break;
			case JSON_ERROR_CTRL_CHAR:
				echo ' - Unexpected control character found';
				break;
			case JSON_ERROR_SYNTAX:
				echo ' - Syntax error, malformed JSON';
				break;
			case JSON_ERROR_UTF8:
				echo ' - Malformed UTF-8 characters, possibly incorrectly encoded';
				break;
			default:
				echo ' - Unknown error';
				break;
		}		
	}
	
	function checkIfEntryExists($id, $pdo, $table) {
		
		$query = "SELECT `id` FROM `" . $table . "` WHERE `id` LIKE " . $id ;					
						
		$stmt=$pdo->prepare($query);			
		$stmt->closeCursor();	
		$stmt->execute();	
		
		$res = $stmt->fetch(PDO::FETCH_ASSOC);
		
		//DEBUG
		// echo "Checkuser for ID: " . $id;
		// echo "\r\n";
		// var_dump($res);
		// echo "\r\n";
		
		if ($res == false) {
			return false;
		}
		
		if (count($res) > 0) {
			return true;
		}
		else {
			return false;
		}		
	}
?>