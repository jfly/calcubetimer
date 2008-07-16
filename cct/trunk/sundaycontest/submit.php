<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Sunday Contest Submission Page</title>
<link href="images/cube1011.css" rel="stylesheet" />
<style type="text/css">
body { font-family:arial, verdana, sans-serif; }
h1 { font-family: Trebuchet MS; color: #006699; text-align: center; }
select { border: 1px solid #000000; width: 100%; }
textarea { border: 1px solid #000000; width: 100%; }
input.text { width: 100%; border: 1px solid #000000; }
td { padding: 0px 0px 6px 5px; }
</style>
<script type="text/javascript">
<!--
var locked = false;
function validateAll() {
	if(document.getElementById("name") == null)
		return;
	if(locked) return; //this is to keep ie from infinite looping when setting border's colors
	locked = true;
	
	var valid = validateEmail();
	valid = validateName() && valid;
	valid = validateAverage() && valid;
	valid = validateTimes() && valid;
	valid = validateQuote() && valid;
	document.getElementById("submit").disabled = !valid;
	
	locked = false;
}
function validateName() {
	var name = document.getElementById("name");
	var empty = (name.value == "");
	document.getElementById("nameresult").innerHTML = empty ? "Please enter your name." : "";
	name.style.border = getBorder(!empty);
	return !empty;
}
function validateEmail() {
	var email = document.getElementById("email");
	var empty = (email.value == "");
	document.getElementById("emailresult").innerHTML = empty ? "Please enter your email address." : "";
	email.style.border = getBorder(!empty);
 	return !empty;
}
function validateQuote() {
	var quote = document.getElementById("quote");
	var empty = (quote.value == "");
	document.getElementById("quoteresult").innerHTML = empty ? "Please enter a quote." : "";
	quote.style.border = getBorder(!empty);
 	return !empty;
}
function validateAverage() {
	var average = document.getElementById("average");
	var div = document.getElementById("averageresult");
	div.innerHTML = "";
	var valid = true;
	if(average.value == "") {
		div.innerHTML = "Please enter an average.";
		valid = false;
	} else if(!isTime(average.value)) {
		div.innerHTML = "Average must be a positive number!";
		valid = false;
	}
	average.style.border = getBorder(valid);
	return valid;
}
function getBorder(valid) {
	return valid ? "1px solid #000000" : "1px solid #ff0000";
}
function isTime(time) {
	return time.match(/^\d+(\.\d+)?$/) != null;
}
function validateTimes() {
	var times = document.getElementById("times");
	var div = document.getElementById("timesresult");
	var timeArray = times.value.split(/, /);
	var i;
	for(i = 0; i < timeArray.length; i++) {
		var time = timeArray[i].replace(/\(|\)|\+/g,""); //remove all parens
		if(!isTime(time) && time != "POP" && time != "DNF" && time != "DNS") { 
			break;
		}
	}
	var valid = false;
	if(i == timeArray.length) { //all the times were valid
		if(i < 12) {
			div.innerHTML = "Too few times!";
		} else if(i > 13) {
			div.innerHTML = "Too many times!";
		} else { //perfect
			valid = true;
			div.innerHTML = "";
		}
	} else if(timeArray.length == 1 && timeArray[0] == "") {
		div.innerHTML = "Please enter your times.";
	} else {
		div.innerHTML = timeArray[i] + " is not a valid time!";
	}
	times.style.border = getBorder(valid);
	return valid;
}
function countrySelected() {
	var country = document.getElementById("country");
	if(country == null)
		return;
	var other = document.getElementById("othercountry");
	if(country.value == "Other") {
		other.innerHTML = "<input type='text' class='text' value='<? if( isset( $_POST['ocountry'] ) ) echo $_POST['ocountry'] ?>' id='ocountry' name='ocountry' style='width: 150px' title='Enter your country' />";
	} else {
		other.innerHTML = "";
	}
}
function changeInputs() {
	var els = document.getElementsByTagName('input');
	var elsLen = els.length;
	var i = 0;
	for(i=0; i<elsLen; i++) {
		if(els[i].getAttribute('type')) {
			if ( els[i].getAttribute('type') == "text" )
				els[i].className = 'text';
			else
				els[i].className = 'button';
		}
	}
}
-->
</script>
</head>
<body onload="validateAll(); countrySelected(); changeInputs();">
<h1>
<img src="images/cubecoolsmall.gif" alt="cube" border="0" height="47" width="43" />The Sunday Contest<img src="images/cubecoolsmall.gif" alt="cube" border="0" height="47" width="43" />
</h1>
<div id="results">
<? 
if( isset( $_POST['submit'] ) )
{
	$isGood = true;
	if( !isset( $_POST['name'] ) || $_POST['name'] == "" )
	{
		echo "No name specified.<br />";
		$isGood = false;
	}
	if( !isset( $_POST['country'] ) || $_POST['country'] == "" )
	{
		echo "No country specified.<br />";
		$isGood = false;
	}
	if( !isset( $_POST['email'] ) || $_POST['email'] == "" )
	{
		echo "No email specified.<br />";
		$isGood = false;
	}
	if( !isset( $_POST['average'] ) || $_POST['average'] == "" )
	{
		echo "No average specified.<br />";
		$isGood = false;
	} elseif( !is_numeric( $_POST['average'] ) ){
		echo "Average is not a number.<br />";
		$isGood = false;
	}
	if( !isset( $_POST['times'] ) || $_POST['times'] == "" )
	{
		echo "No times specified.<br />";
		$isGood = false;
	} else {
		$tmp = explode( ", ", $_POST['times'] );
		$tmp = str_replace( "(", "", $tmp );
		$tmp = str_replace( ")", "", $tmp );
		$tmp = str_replace( "+", "", $tmp );
		foreach( $tmp as $time )
			if( !is_numeric( $time ) && stripos( $time, "pop" ) === false && stripos( $time, "dnf" ) === false && stripos( $time, "dns" ) === false ) {
				echo "\"".$time."\" is not numeric.<br />";
				$isGood = false;
			}
		if( sizeof( $tmp ) < 12 || sizeof( $tmp ) > 13 )
		{
			echo "Wrong number of times.<br />";
			$isGood = false;
		}
	}
	if( !isset( $_POST['quote'] ) || $_POST['quote'] == "" )
	{
		echo "No quote specified.<br />";
		$isGood = false;
	}
	if( $isGood )
	{
		// todo: check to see if name already exists in file
		$date = getdate();
		if( $date[wday] == 0 ) $date[wday] = 7;
		$date = strtotime( "+".(7-$date[wday])." days", $date[0] );
		$date = date( "Y-n-j", $date );
		$country = $_POST['country'];
		if( $country == "Other" )
		{
			if ( isset( $_POST['ocountry'] ) && $_POST['ocountry'] != "" ) {
				$country = $_POST['ocountry'];
			} else {
				echo "Enter your country's name: <form action=\"".$_SERVER['PHP_SELF']."\" method=\"post\">";
				foreach( $_POST as $key => $value )
					echo "<input type=\"hidden\" name=\"".$key."\" value=\"".$value."\" />";
				die( "<input type=\"text\" name=\"country\" /><input type=\"submit\" name=\"submit\" value=\"Continue\" /></form></div></body></html>" );
			}
		}
		if( substr( $_POST['quote'], -1 ) == "\\" ) $_POST['quote'] .= " ";
		$submission = array( $_POST['name'], $country, $_POST['email'], $_POST['average'], $_POST['times'], $_POST['quote'], ( isset( $_POST['showemail'] ) ? "true" : "false" ), $_SERVER['REMOTE_ADDR'] );
		$fp = "";
		if( file_exists( $date.".csv" ) === false ) {
			$fp = fopen( $date.".csv", "wb" );
			fputcsv( $fp, array( ) );
		} else $fp = fopen( $date.".csv", "ab" );
		echo "<br />";
		fputcsv( $fp, $submission );
		fclose( $fp );
		echo $date."<br />";
		echo "submitted successfully!</div></body></html>";
		die();
	}
}
?>
</div>
<p>Results for <? 
$date = getdate();
if( $date[wday] == 0 ) $date[wday] = 7;
$date = strtotime( "+".(7-$date[wday])." days", $date[0] );
echo date( "Y-n-j", $date );
?></p>
<p>Use the form below to enter your times. The form will be
submitted to the server for processing. Please make sure that your submission
conforms to the <a href="rules.htm">Rules</a>. Note all times must be in seconds.</p>

<a name="form" />
<form id="frm" name="frm" method="post" action="<? echo $_SERVER['PHP_SELF']; ?>">
<table>
<tr>
	<td><label for="name">Name</label></td>
	<td style="width: 300px"><input type="text"
		id="name" 
		name="name"
		title="Enter your full name"
		autocomplete="off"
		onpropertychange="validateAll()"
		oninput="validateAll()"
		<? if( isset( $_POST['name'] ) ) echo " value=\"".$_POST['name']."\""; ?> /></td>
		<td><div id="nameresult"></div></td>
	<td></td>
</tr>
<tr>
	<td><label for="country">Country</label></td>
	<td><?
		echo "<select onchange='countrySelected()' id='country' name='country' title='Enter your country'>";
			$countries = array( "USA", "UK", "Thailand", "Taiwan", "Switzerland", "Sweden", "Spain", "South Africa", "Singapore", "South Korea", "Puerto Rico",
							"Portugal", "Poland", "The Philippines", "Pakistan", "Norway", "New Zealand", "The Netherlands", "Mexico", "Malaysia", "Japan", "Ivory Coast",
							"Italy", "Israel", "Indonesia", "India", "Hong Kong", "Guam", "Germany", "France", "Finland", "The Dominican Republic", "Denmark", "Cuba",
							"China", "Canada", "Brazil", "Belgium", "Australia", "Austria", "Aruba", "Other" );
			foreach( $countries as $country ) {
				$selected = "";
				if( isset( $_POST['country'] ) && $country == $_POST['country'] ) 
					$selected = "selected='selected'";
				echo "<option value='".$country."'".$selected.">".$country."</option>";
			}
		echo "</select>";
	?></td>
	<td id="othercountry"></td>
</tr>
<tr>
	<td><label for="email">Email</label></td>
	<td><input type="text"
		id="email"
		name="email"
		title="Enter your email address"
		autocomplete="off"
		onpropertychange="validateAll()"
		oninput="validateAll()"
		<? if( isset( $_POST['email'] ) ) echo " value=\"".$_POST['email']."\""; ?> /></td>
	<td><div id="emailresult"></div></td>
</tr>
<tr>
	<td><label for="average">Average</label></td>
	<td><input type="text"
		id="average"
		name="average"
		title="Average"
		autocomplete="off"
		onpropertychange="validateAll()"
		oninput="validateAll()"
		<? if( isset( $_POST['average'] ) ) echo " value=\"".$_POST['average']."\""; ?> /></td>
	<td><div id="averageresult"></div></td>
</tr>
<tr>
	<td><label for="times">Times</label></td>
	<td><textarea id="times"
			name="times"
			cols="60"
			rows="2"
			autocomplete="off"
			onpropertychange="validateAll()"
			oninput="validateAll()"
			title="Enter your 12 or 13 times in standard notation"><? if( isset( $_POST['times'] ) ) echo $_POST['times']; ?></textarea></td>
	<td><div id="timesresult"></div></td>
</tr>
<tr>
	<td><label for="quote">Quote</label></td>
	<td><textarea id="quote"
			name="quote"
			cols="60"
			rows="2"
			autocomplete="off"
			onpropertychange="validateAll()"
			oninput="validateAll()"
			title="Enter a quote if you like"><? if( isset( $_POST['quote'] ) ) echo $_POST['quote']; ?></textarea></td>
	<td><div id="quoteresult"></div></td>
</tr>
<tr>
	<td></td>
	<td><input class="check" id="showemail" name="showemail" title="Would you like your name shown to link to your email address?" type="checkbox"<? if( isset( $_POST['showemail'] ) ) echo " checked=\"checked\""; ?> />
	<label for="showemail">Show email address?</label></td>
	<td></td>
</tr>
<tr>
	<td></td>
	<td>
	<input class="check" id="submit" name="submit" value="Submit times" title="Click to submit" type="submit" />
	<input type="button" value="Reset" onclick="document.getElementById('frm').reset();validateAll()" />
	</td>
	<td></td>
</tr>
</table>
</form>
</body>
</html>
