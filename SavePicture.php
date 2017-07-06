<?php
	
	if (isset($_POST["name"]) && isset($_POST["image"])) {

	$name = $_POST["name"];
	$image = $_POST["image"];
	$imageName = $name.".jpg";
	$filePath = "Uploaded Images/".$imageName;
	//echo "file".$filePath;	
	//$decodedImage = base64_decode($image);
	//file_put_contents($filePath, base64_decode($image));

	if(file_exists($filePath)){
		unlink($filePath);
	}
	$nyfile = fopen($filePath, "w") or die("Unable to open file");
	file_put_contents($filePath, base64_decode($image));
	}else {
		echo "Not Set";
	}
?>