<%--

    Copyright 2008 The University of North Carolina at Chapel Hill

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="header.jsp"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="icon" href="<c:url value='/favicon.ico'/>" type="image/x-icon" />
<title><fmt:message key="updateobject.heading"/></title>
<LINK REL=StyleSheet HREF="<c:url value='/css/unc_styles.css'/>"
	TYPE="text/css" />
<LINK REL=StyleSheet HREF="<c:url value='/css/ir_style.css'/>"
	TYPE="text/css" />

  <script src="http://code.jquery.com/jquery-latest.js"></script>

</head>
<body>

<form id="myForm">
<div id="titleInfo">
</div>
<br/>
    <div>
        <input type="button" id="titleInfoAdd" value="Add titleInfo" />
    </div>
<br/>
<div id="name"/>
</div>
<br/>
    <div>
        <input type="button" id="nameAdd" value="Add name" />
    </div>
<br/>
<div id="typeOfResource"></div>
<br/>
    <div>
        <input type="button" id="typeOfResourceAdd" value="Add typeOfResource" />
    </div>
<br/>
<div id="genre"></div>
<br/>
    <div>
        <input type="button" id="genreAdd" value="Add genre" />
    </div>
<br/>
<div id="originInfo"></div>
<br/>
    <div>
        <input type="button" id="originInfoAdd" value="Add originInfo" />
    </div>
<br/>
<div id="language"></div>
<br/>
    <div>
        <input type="button" id="languageAdd" value="Add language" />
    </div>
<br/>
<div id="physicalDescription"></div>
<br/>
    <div>
        <input type="button" id="physicalDescriptionAdd" value="Add physicalDescription" />
    </div>
<br/>
<div id="abstract"></div>
<br/>
    <div>
        <input type="button" id="abstractAdd" value="Add abstract" />
    </div>
<br/>
<div id="tableOfContents"></div>
<br/>
    <div>
        <input type="button" id="tableOfContentsAdd" value="Add tableOfContents" />
    </div>
<br/>
<div id="targetAudience"></div>
<br/>
    <div>
        <input type="button" id="targetAudienceAdd" value="Add targetAudience" />
    </div>
<br/>
<div id="note"></div>
<br/>
    <div>
        <input type="button" id="noteAdd" value="Add note" />
    </div>
<br/>
<div id="subject"></div>
<br/>
    <div>
        <input type="button" id="subjectAdd" value="Add subject" />
    </div>
<br/>
<div id="classification"></div>
<br/>
    <div>
        <input type="button" id="classificationAdd" value="Add classification" />
    </div>
<br/>
<div id="relatedItem"></div>
<br/>
    <div>
        <input type="button" id="relatedItemAdd" value="Add relatedItem" />
    </div>
<br/>
<div id="identifier"></div>
<br/>
    <div>
        <input type="button" id="identifierAdd" value="Add identifier" />
    </div>
<br/>
<div id="location"></div>
<br/>
    <div>
        <input type="button" id="locationAdd" value="Add location" />
    </div>
<br/>
<div id="accessCondition"></div>
<br/>
    <div>
        <input type="button" id="accessConditionAdd" value="Add accessCondition" />
    </div>
<br/>
<div id="part"></div>
<br/>
    <div>
        <input type="button" id="partAdd" value="Add part" />
    </div>
<br/>
<div id="extension"></div>
<br/>
    <div>
        <input type="button" id="extensionAdd" value="Add extension" />
    </div>
<br/>
<div id="recordInfo"></div>
<br/>
    <div>
        <input type="button" id="recordInfoAdd" value="Add recordInfo" />
    </div>
<br/>

<input type="button" id="sendXML" value="Submit Changes" />
</form>



<script>
$(document).ready(function()
{

window.MyVariables = {};
window.MyVariables.xml = {};

  $.ajax({
    type: "GET",
    url: "https://nagina/cdradmin/modsexample.xml",
    dataType: "xml",
    success: function(xml) { setupEditor(xml); }
  });


// set up button callbacks
$('#titleInfoAdd').click(function() { addTitleInfoElements(); });
$('#nameAdd').click(function() { addNameElements(); });
$('#typeOfResourceAdd').click(function() { addTypeOfResourceElements(); });
$('#genreAdd').click(function() { addGenreElements(); });
$('#originInfoAdd').click(function() { addOriginInfoElements(); });
$('#languageAdd').click(function() { addLanguageElements(); });
$('#physicalDescriptionAdd').click(function() { addPhysicalDescriptionElements(); });
$('#abstractAdd').click(function() { addAbstractElements(); });
$('#tableOfContentsAdd').click(function() { addTableOfContentsElements(); });
$('#targetAudienceAdd').click(function() { addTargetAudienceElements(); });
$('#noteAdd').click(function() { addNoteElements(); });
$('#subjectAdd').click(function() { addSubjectElements(); });
$('#classificationAdd').click(function() { addClassificationElements(); });
$('#relatedItemAdd').click(function() { addRelatedItemElements(); });
$('#identifierAdd').click(function() { addIdentifierElements(); });
$('#locationAdd').click(function() { addLocationElements(); });
$('#accessConditionAdd').click(function() { addAccessConditionElements(); });
$('#partAdd').click(function() { addPartElements(); });
$('#extensionAdd').click(function() { addExtensionElements(); });
$('#recordInfoAdd').click(function() { addRecordInfoElements(); });

$('#sendXML').click(function() { sendXML(); });


}); // document ready


// Method to create elements
function createElement(element, parentElement, count, containerId, indent) {
	var existingElement = false;
	var cleanContainerId = containerId.substring(1);	
	var elementContainerId = cleanContainerId+'_'+element.title+'Instance'+count;

	$('<div/>').attr({'id' : elementContainerId, 'class' : element.title+'Instance'}).appendTo(containerId);

	// See if element already exists.  If not, create it and add it to xml document
	var numElements = $(parentElement).children(element.title).length
	//alert("numElements: "+numElements+' count: '+count);
	if( numElements > count ) {
		existingElement = true;
	} else {
		//alert("creating element in XML: "+element.title);
		$('<'+element.title+'/>').appendTo(parentElement);
	}

	// set up element title and entry field if appropriate
	if(element.type == 'none') {
		createElementText(element.title, '#'+elementContainerId);
	} else {
		var valueValue = '';
		if(existingElement) {
			valueValue = $(parentElement).children(element.title).eq(count).text();
		}
		createElementLabelAndInput(element, cleanContainerId+'_'+element.title, valueValue, '#'+elementContainerId, count, parentElement);
	}

	$('<input>').attr({'type' : 'button', 'value' : 'X', 'id' : cleanContainerId+'_'+element.title+'Del'+count}).appendTo('#'+elementContainerId);
	
	$('#'+cleanContainerId+'_'+element.title+'Del'+count).on('click', { value : count }, function(event) {
		
		// delete selected titleInfo from XML
		$(parentElement).children(element.title).eq(event.data.value).remove();

		// redisplay titleInfo listing
		$(containerId).children("."+element.title+'Instance').remove();
		$(parentElement).children(element.title).each(function() { 
			var num = $(containerId > '.'+element.title+'Instance').length; 
	
			if(num == undefined) num = 0;

			createElement(element, parentElement, num, containerId, indent);
		});
	 });



	// add attributes
	var attributesArray = element.attributes;
	var hasAttributes = (attributesArray.length > 0 ? true : false);

	if(hasAttributes) {
		// add attribute div show/hide button
		$('<input>').attr({'type' : 'button', 'value' : 'Attributes', 'id' : elementContainerId+'_attrs'}).appendTo('#'+elementContainerId);
		$('#'+elementContainerId+'_attrs').on('click', function() { 
			$('#'+elementContainerId+'_attrsDiv').toggle();
		});	
	}

	// add element buttons
	var elementsArray = element.elements;
	var hasElements = (elementsArray.length > 0 ? true : false);

	if(hasElements) {
		// add element div show/hide button
		$('<input>').attr({'type' : 'button', 'value' : 'Subelements', 'id' : elementContainerId+'_elements'}).appendTo('#'+elementContainerId);
		$('#'+elementContainerId+'_elements').on('click', function() { 
			$('#'+elementContainerId+'_elementsDiv').toggle();
		});	
	}
	$('<div/>').attr({'id' : elementContainerId+'_elementsDiv'}).appendTo('#'+elementContainerId).hide();

	for (var i = 0; i < elementsArray.length; i++) {
		addElementButton(element, elementContainerId+'_elementsDiv', parentElement, elementsArray[i], count, indent);
	}

	// attribute div	
	if(hasAttributes) {
		// add attribute div hidden
		$('<br/>').appendTo('#'+elementContainerId);
		$('<div/>').attr({'id' : elementContainerId+'_attrsDiv'}).appendTo('#'+elementContainerId).hide();

		// populate attribute div with attribute entry fields
		for (var i = 0; i < attributesArray.length; i++) {				
			createAttribute(elementContainerId+"_"+attributesArray[i].title, attributesArray[i], parentElement, element.title, count, '#'+elementContainerId+'_attrsDiv', 2);
			$('<br/>').appendTo('#'+elementContainerId+'_attrsDiv');
		}
	}

	// add elements
	var elementsArray = element.elements;
	for (var i = 0; i < elementsArray.length; i++) {
		var elementCount = $(parentElement).children(element.title).eq(count).children(elementsArray[i].title).length;
	
		for(var j = 0; j < elementCount; j++) {
			createElement(elementsArray[i], $(parentElement).children(element.title).eq(count), j, '#'+elementContainerId, 4);
		}
	}

	
	$('<br/><br/>').appendTo('#'+elementContainerId);
}


function addElementButton(element, elementContainerId, parentElement, childElement, count, indent) {

		if(childElement == undefined) alert(element.title+' '+count);

		$('<input>').attr({'type' : 'button', 'value' : 'Add '+childElement.title, 'id' : elementContainerId+'_'+childElement.title+'_Add'}).appendTo('#'+elementContainerId);

		$('#'+elementContainerId+'_'+childElement.title+'_Add').on('click', addElementButtonCallback(element, elementContainerId, parentElement, childElement, count, indent));
}

function addElementButtonCallback(element, elementContainerId, parentElement, childElement, count, indent) {
	return function() {	
		
		var num = $('#'+elementContainerId).children("."+childElement.title+'Instance').length; 

		if(num == undefined) num = 0; // if no elements, start with zero

		createElement(childElement, $(parentElement).children(element.title).eq(count), num, '#'+elementContainerId, indent);
	}
}


function createAttribute(idValue, attributeValue, parentValue, nameValue, countValue, appendValue, indentValue) {
	$('<label/>').attr({'for' : idValue }).text(attributeValue.title).appendTo(appendValue);

	var value = $(parentValue).children(nameValue).eq(countValue).attr(attributeValue.title);

	if(value) {
		; // Should I do any cleanup/formatting?
		
	} else if(attributeValue.defaultValue) {
		value = attributeValue.defaultValue;
	} else value = '';

	if(attributeValue.type == 'text') {
		$('<input/>').attr({'id' : idValue, 'type' : 'text', 'name' : attributeValue.title, 'value' : value}).appendTo(appendValue);
	} else if(attributeValue.type == 'selection') {

		var selectionValues = attributeValue.values;

		var s = $('<select />').attr({'id' : idValue, 'name' : attributeValue.title, 'value' : value}).appendTo(appendValue);

		for(var val in selectionValues) {

			if(value == selectionValues[val]) {				
	    			$('<option />', {value: selectionValues[val], text: selectionValues[val], selected : true}).appendTo(s);
			} else {
    				$('<option />', {value: selectionValues[val], text: selectionValues[val]}).appendTo(s);
			}
		}
	}

       // Change	
	$('#'+idValue).on('change', function(event) {	
		if($('#'+idValue).val()) {
			$(parentValue).children(nameValue).eq(countValue).attr(attributeValue.title, $('#'+idValue).val());
		} else {
			// remove empty attribute
			$(parentValue).children(nameValue).eq(countValue).removeAttr(attributeValue.title);
		}
	});
}

function createElementText(nameValue, appendValue) {
		$('<p/>').text(nameValue+' ').appendTo(appendValue);
}

function createElementLabel(forValue, nameValue, appendValue) {
		$('<label/>').attr({'for' : forValue }).text(nameValue+' ').appendTo(appendValue);
}

function createElementInput(element, idValue, valueValue, appendValue) {
	if(element.type == 'text') {
		$('<input/>').attr({'id' : idValue, 'type' : element.type, 'name' : element.title, 'value' : valueValue}).appendTo(appendValue);
	} else if(element.type == 'textarea') {
		$('<textarea/>').attr({'id' : idValue, 'name' : element.title, 'value' : valueValue}).appendTo(appendValue);
	} else if(element.type == 'selection') {

		var selectionValues = element.values;

		var s = $('<select />').attr({'id' : idValue, 'name' : element.title, 'value' : valueValue}).appendTo(appendValue);

		for(var val in selectionValues) {

			if(valueValue == selectionValues[val]) {				
	    			$('<option />', {value: selectionValues[val], text: selectionValues[val], selected : true}).appendTo(s);
			} else {
    				$('<option />', {value: selectionValues[val], text: selectionValues[val]}).appendTo(s);
			}
		}
	}

}

function createElementChangeCallback(element, parentValue, countValue) {
	return function() {
		$(parentValue).children(nameValue).eq(countValue).text($('#'+idValue+event.data.value).val());
	}
}

function createElementLabelAndInput(element, idValue, valueValue, appendValue, countValue, parentValue) {
	createElementLabel(idValue+countValue, element.title, appendValue);
	createElementInput(element, idValue+countValue, valueValue, appendValue);

        // Change	
	$('#'+idValue+countValue).on('change', createElementChangeCallback(element, parentValue, countValue));	
}

function addTitleInfoElements() {

	var num = $('#titleInfo > .titleInfoInstance').length; 
	
	if(num == undefined) num = 0;

	createElement(TitleInfo, $(window.MyVariables.xml).find("mods"), num, '#titleInfo', 2);
}
function addNameElements() {

	var num = $('#name > .nameInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Name, $(window.MyVariables.xml).find("mods"), num, '#name', 2);
}

function addTypeOfResourceElements() {

	var num = $('#typeOfResource > .typeOfResourceInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(TypeOfResource, $(window.MyVariables.xml).find("mods"), num, '#typeOfResource', 2);
}

function addGenreElements() {

	var num = $('#genre > .genreInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Genre, $(window.MyVariables.xml).find("mods"), num, '#genre', 2);
}

function addOriginInfoElements() {

	var num = $('#originInfo > .originInfoInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(OriginInfo, $(window.MyVariables.xml).find("mods"), num, '#originInfo', 2);
}

function addLanguageElements() {

	var num = $('#language > .languageInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Language, $(window.MyVariables.xml).find("mods"), num, '#language', 2);
}

function addPhysicalDescriptionElements() {

	var num = $('#physicalDescription > .physicalDescriptionInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(PhysicalDescription, $(window.MyVariables.xml).find("mods"), num, '#physicalDescription', 2);
}

function addAbstractElements() {

	var num = $('#abstract > .abstractInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Abstract, $(window.MyVariables.xml).find("mods"), num, '#abstract', 2);
}

function addTableOfContentsElements() {

	var num = $('#tableOfContents > .tableOfContentsInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(TableOfContents, $(window.MyVariables.xml).find("mods"), num, '#tableOfContents', 2);
}

function addTargetAudienceElements() {

	var num = $('#targetAudience > .targetAudienceInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(TargetAudience, $(window.MyVariables.xml).find("mods"), num, '#targetAudience', 2);
}
function addNoteElements() {

	var num = $('#note > .noteInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Note, $(window.MyVariables.xml).find("mods"), num, '#note', 2);
}
function addSubjectElements() {

	var num = $('#subject > .subjectInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Subject, $(window.MyVariables.xml).find("mods"), num, '#subject', 2);
}

function addClassificationElements() {

	var num = $('#classification > .classificationInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Classification, $(window.MyVariables.xml).find("mods"), num, '#classification', 2);
}
function addRelatedItemElements() {

	var num = $('#relatedItem > .relatedItemInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(RelatedItem, $(window.MyVariables.xml).find("mods"), num, '#relatedItem', 2);
}
function addIdentifierElements() {

	var num = $('#identifier > .identifierInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Identifier, $(window.MyVariables.xml).find("mods"), num, '#identifier', 2);
}
function addLocationElements() {

	var num = $('#location > .locationInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Location, $(window.MyVariables.xml).find("mods"), num, '#location', 2);
}
function addAccessConditionElements() {

	var num = $('#accessCondition > .accessConditionInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(AccessCondition, $(window.MyVariables.xml).find("mods"), num, '#accessCondition', 2);
}
function addPartElements() {

	var num = $('#part > .partInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Part, $(window.MyVariables.xml).find("mods"), num, '#part', 2);
}
function addExtensionElements() {

	var num = $('#extension > .extensionInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Extension, $(window.MyVariables.xml).find("mods"), num, '#extension', 2);
}
function addRecordInfoElements() {

	var num = $('#recordInfo > .recordInfoInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(RecordInfo, $(window.MyVariables.xml).find("mods"), num, '#recordInfo', 2);
}

function setupEditor(xml)
{
  // make XML accessible to rest of code
  window.MyVariables.xml = xml;

//  $('<'+'fakeElement'+'/>').appendTo($(window.MyVariables.xml).find("mods").children("titleInfo").eq(0));
//  $(window.MyVariables.xml).find("mods").children("titleInfo").eq(0).children("fakeElement").attr({"test" : "one"});
//  $(window.MyVariables.xml).find("mods").children("titleInfo").eq(0).children("fakeElement").removeAttr("test");

  // preload the title
  $(window.MyVariables.xml).find('mods').children("titleInfo").each(function() { addTitleInfoElements(); });
  $(window.MyVariables.xml).find('mods').children("name").each(function() { addNameElements(); });
  $(window.MyVariables.xml).find('mods').children("typeOfResource").each(function() { addTypeOfResourceElements(); });
  $(window.MyVariables.xml).find('mods').children("genre").each(function() { addGenreElements(); });
  $(window.MyVariables.xml).find('mods').children("originInfo").each(function() { addOriginInfoElements(); });
  $(window.MyVariables.xml).find('mods').children("language").each(function() { addLanguageElements(); });
  $(window.MyVariables.xml).find('mods').children("physicalDescription").each(function() { addPhysicalDescriptionElements(); });
  $(window.MyVariables.xml).find('mods').children("abstract").each(function() { addAbstractElements(); });
  $(window.MyVariables.xml).find('mods').children("tableOfContents").each(function() { addTableOfContentsElements(); });
  $(window.MyVariables.xml).find('mods').children("targetAudience").each(function() { addTargetAudienceElements(); });
  $(window.MyVariables.xml).find('mods').children("note").each(function() { addNoteElements(); });
  $(window.MyVariables.xml).find('mods').children("subject").each(function() { addSubjectElements(); });
  $(window.MyVariables.xml).find('mods').children("classification").each(function() { addClassificationElements(); });
  $(window.MyVariables.xml).find('mods').children("relatedItem").each(function() { addRelatedItemElements(); });
  $(window.MyVariables.xml).find('mods').children("identifier").each(function() { addIdentifierElements(); });
  $(window.MyVariables.xml).find('mods').children("location").each(function() { addLocationElements(); });
  $(window.MyVariables.xml).find('mods').children("accessCondition").each(function() { addAccessConditionElements(); });
  $(window.MyVariables.xml).find('mods').children("part").each(function() { addPartElements(); });
  $(window.MyVariables.xml).find('mods').children("extension").each(function() { addExtensionElements(); });
  $(window.MyVariables.xml).find('mods').children("recordInfo").each(function() { addRecordInfoElements(); });

}

// Send XML back to be stored
function sendXML() {
	if( !window.XMLSerializer ){
	   window.XMLSerializer = function(){};

	   window.XMLSerializer.prototype.serializeToString = function( XMLObject ){
	      return XMLObject.xml || '';
	   };
	}

	// convert XML DOM to string
	var xmlString = xml2Str(window.MyVariables.xml);

        var str = '<?xml version="1.0" encoding="UTF-8"?><foo><bar>Hello World</bar></foo>';
        // var xmlData = strToXml(str); // no need for this unless you want to use it
                                        // on client side
        // console.log($.isXMLDoc(xmlData)); 
        $.ajax({
           url: 'https://nagina/cdradmin/ir/admin/modsform',
           contentType: "application/xml",
           type: "POST",  // type should be POST
           data: xmlString, // send the string directly
           success: function(response){
             alert(response);
           },
           error: function(response) {
              alert(response);
           }
        });
}


function xml2Str(xmlNode)
{
  try {
    // Gecko-based browsers, Safari, Opera.
    return (new XMLSerializer()).serializeToString(xmlNode);
  }
  catch (e) {
    try {
      // Internet Explorer.
      return xmlNode.xml;
    }
    catch (e)
    {//Strange Browser ??
     alert('Xmlserializer not supported');
    }
  }
  return false;
}

placeTerm_type_attr, placeTerm_authority_attr

var collection_attr = {
	title : 'collection',
	type : 'text',
	defaultValue : 'yes',
	values : []
}

var manuscript_attr = {
	title : 'manuscript',
	type : 'text',
	defaultValue : 'yes',
	values : []
}

var nameTitleGroup_attr = {
	title : 'nameTitleGroup',
	type : 'text',
	defaultValue : null,
	values : []
}

var altRepGroup_attr = {
	title : 'altRepGroup',
	type : 'text',
	defaultValue : null,
	values : []
}

var usage_attr = {
	title : 'usage',
	type : 'text',
	defaultValue : 'primary',
	values : []
}

var supplied_attr = {
	title : 'supplied',
	type : 'text',
	defaultValue : 'yes',
	values : []
}

var displayLabel_attr = {
	title : 'displayLabel',
	type : 'text',
	defaultValue : null,
	values : []
}

var valueURI_attr = {
	title : 'valueURI',
	type : 'text',
	defaultValue : null,
	values : []
}

var authorityURI_attr = {
	title : 'authorityURI',
	type : 'text',
	defaultValue : null,
	values : []
}

var genre_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}


var authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}

var transliteration_attr = {
	title : 'transliteration',
	type : 'text',
	defaultValue : null,
	values : []
}

var script_attr = {
	title : 'script',
	type : 'text',
	defaultValue : null,
	values : []
}

var xmllang_attr = {
	title : 'xml:lang',
	type : 'text',
	defaultValue : null,
	values : []
}

var lang_attr = {
	title : 'lang',
	type : 'text',
	defaultValue : null,
	values : []
}

var xlink_attr = {
	title : 'xlink',
	type : 'text',
	defaultValue : null,
	values : []
}

var ID_attr = {
	title : 'ID',
	type : 'text',
	defaultValue : null,
	values : []
}

var placeTerm_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','code', 'text']
}

var placeTerm_authority_attr = {
	title : 'authority',
	type : 'selection',
	defaultValue : null,
	values : ['','marcgac', 'marcountry', 'iso3166']
}

var languageTerm_authority_attr = {
	title : 'authority',
	type : 'selection',
	defaultValue : null,
	values : ['', 'iso639-2b', 'rfc3066', 'iso639-3', 'rfc4646']
}

var scriptTerm_authority_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','marcgac', 'marcountry', 'iso3166']
}

var targetAudience_authority_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','adolescent', 'adult', 'general', 'juvenile', 'preschool', 'specialized']
}

var titleInfo_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','abbreviated', 'translated', 'alternative', 'uniform']
}

var name_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','personal', 'corporate', 'conference', 'family']
}

var namePart_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','date', 'family', 'given', 'termsOfAddress']
}

var roleTerm_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','code', 'text']
}

var languageTerm_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','code', 'text']
}

var scriptTerm_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','code', 'text']
}

var genre_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','class', 'work type', 'style']
}

var part_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','volume','issue','chapter','section','paragraph','track']
}

var part_text_type_attr = {
	title : 'type',
	type : 'text',
	defaultValue : null,
	values : [ ]
}

var detail_title_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','part','volume','issue','chapter','section','paragraph','track']
}


var unit_attr = {
	title : 'unit',
	type : 'selection',
	defaultValue : null,
	values : ['','pages','minutes']
}

var level_attr = {
	title : 'level',
	type : 'text',
	defaultValue : null,
	values : []
}

var order_attr = {
	title : 'order',
	type : 'text',
	defaultValue : null,
	values : []
}


var encoding_attr = {
	title : 'encoding',
	type : 'selection',
	defaultValue : null,
	values : ['','w3cdtf', 'iso8601','marc','edtf','temper']
}
var point_attr = {
	title : 'point',
	type : 'selection',
	defaultValue : null,
	values : ['','start','end']
}
var keyDate_attr = {
	title : 'keyDate',
	type : 'text',
	defaultValue : 'yes',
	values : []
}
var qualifier_attr = {
	title : 'qualifier',
	type : 'selection',
	defaultValue : null,
	values : ['','approximate','inferred','questionable']
}
var frequency_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}
var objectPart_attr = {
	title : 'objectPart',
	type : 'text',
	defaultValue : null,
	values : []
}

var subject_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}

var topic_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}

var geographic_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}

var temporal_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}

var geographicCode_authority_attr = {
	title : 'authority',
	type : 'selection',
	defaultValue : null,
	values : ['', 'marcgac', 'marccountry','iso3166']
}

var hierarchicalGeographic_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}

var occupation_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}

var physicalLocation_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}


var classification_authority_attr = {
	title : 'authority',
	type : 'selection',
	defaultValue : null,
	values : ['','accs','acmccs','agricola','agrissc','anscr','ardocs','asb','azdocs','bar','bcl','bcmc','bisacsh','bkl','bliss','blissc','blsrissc','cacodoc','cadocs','ccpgq','celex','chfbn','clc','clutscny','codocs','cslj','cstud',
'cutterec','ddc','dopaed','egedeklass','ekl','farl','farma','fcps','fiaf','finagri','flarch','fldocs','frtav','gadocs','gfdc','ghbs','iadocs','ifzs','inspec','ipc','jelc','kab','kfmod','kktb','knt','ksdocs','kssb','kuvacs','laclaw','ladocs',
'lcc','loovs','methepp','midocs','mmlcc','mf-class','modocs','moys','mpkkl','msc','msdocs','mu','naics','nasasscg','nbdocs','ncdocs','ncsclt','nhcp','nicem','niv','njb','nlm','nmdocs','no-ujur-cmr','no-ujur-cnip','no-ureal-ca','no-ureal-cb',
'no-ureal-cg','noterlyd','nvdocs','nwbib','nydocs','ohdocs','okdocs','oosk','ordocs','padocs','pssppbkj','rich','ridocs','rilm','rpb','rswk','rubbk','rubbkd','rubbkk','rubbkm','rubbkmv','rubbkn','rubbknp','rubbko','rubbks','rueskl','rugasnti',
'rvk','sbb','scdocs','sddocs','sdnb','sfb','siblcs','skb','smm','ssd','ssgn','sswd','stub','suaslc','sudocs','swank','taikclas','taykl','teatkl','txdocs','tykoma','ubtkl/2','udc','uef','undocs','upsylon','usgslcs','utk','utklklass','utklklassex',
'utdocs','veera','vsiso','wadocs','widocs','wydocs','ykl','z','zdbs']
}

var edition_attr = {
	title : 'edition',
	type : 'text',
	defaultValue : null,
	values : []
}


var form_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}
var form_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['', 'material', 'technique']
}

var ci_form_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : []
}

var ci_form_type_attr = {
	title : 'type',
	type : 'text',
	defaultValue : null,
	values : [ ]
}

var ci_note_type_attr = {
	title : 'type',
	type : 'text',
	defaultValue : null,
	values : [ ]
}

var dateLastAccessed_attr = {
	title : 'type',
	type : 'text',
	defaultValue : null,
	values : [ ]
}

var temporal_encoding_attr = {
	title : 'encoding',
	type : 'selection',
	defaultValue : null,
	values : ['', 'w3cdtf','iso8601','marc','edtf','temper']
}

var temporal_qualifier_attr = {
	title : 'qualifier',
	type : 'selection',
	defaultValue : null,
	values : ['', 'approximate','inferred','questionable']
}


var note_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['', 'condition', 'marks', 'medium', 'organization', 'physical description', 'physical details', 'presentation', 'script', 'support', 'technique']
}

var primary_note_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['', 'accrual method','accrual policy','acquisition','action','additional physical form','admin','bibliographic history','bibliography','biographical/historical','citation/reference','conservation history','content',
'creation/production credits','date','exhibitions','funding','handwritten','language','numbering','date/sequential designation','original location','original version','ownership','performers','preferred citation','publications',
'reproduction','restriction','source characteristics','source dimensions','source identifier','source note','source type','statement of responsibility','subject completeness','system details','thesis','venue','version identification' ]
}

var tableOfContents_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['', 'incomplete contents', 'partial contents']
}

var abstract_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['', 'review', 'scope', 'content']
}

var relatedItem_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['', 'preceding','succeeding','original','host','constituent', 'series','otherVersion','otherFormat','isReferencedBy','references','reviewOf']
}

var identifier_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['', 'hdl','doi','isbn','isrc','ismn','issn','issue number','istc','lccn','local','matrix number','music number','music publisher','music plate','sici','uri','upc','videorecording identifier','stock number']
}

var accessCondition_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['', 'restriction on access', 'use and reproduction']
}

var physicalLocation_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['', 'current','discovery','former','creation']
}

var url_access_attr = {
	title : 'access',
	type : 'selection',
	defaultValue : null,
	values : ['', 'preview','raw object','object in context']
}

var url_note_attr = {
	title : 'note',
	type : 'text',
	defaultValue : null,
	values : [ ]
}

var url_usage_attr = {
	title : 'usage',
	type : 'selection',
	defaultValue : null,
	values : ['', 'primary display', 'primary']
}

var unitType_attr = {
	title : 'unitType',
	type : 'selection',
	defaultValue : null,
	values : ['', '1','2','3']
}

var invalid_attr = {
	title : 'invalid',
	type : 'text',
	defaultValue : 'yes',
	values : []
}


var shareable_attr = {
	title : 'shareable',
	type : 'text',
	defaultValue : 'no',
	values : []
}

var recordContentSource_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : [ ]
}

var descriptionStandard_authority_attr = {
	title : 'authority',
	type : 'text',
	defaultValue : null,
	values : [ ]
}

var source_attr = {
	title : 'source',
	type : 'text',
	defaultValue : null,
	values : [ ]
}

var Title = {
	title : 'title',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var SubTitle = {
	title : 'subTitle',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var PartNumber = {
	title : 'partNumber',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var PartName = {
	title : 'partName',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var NonSort = {
	title : 'nonSort',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var TitleInfo = {
	title : 'titleInfo',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ ID_attr, xlink_attr, xmllang_attr, script_attr, transliteration_attr, titleInfo_type_attr, authority_attr, authorityURI_attr, valueURI_attr, displayLabel_attr, supplied_attr, usage_attr, altRepGroup_attr, nameTitleGroup_attr ],
	elements : [ Title, SubTitle, PartNumber, PartName, NonSort ]
};

var NamePart = {
	title : 'namePart',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ namePart_type_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var DisplayForm = {
	title : 'displayForm',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Affiliation = {
	title : 'affiliation',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var RoleTerm = {
	title : 'roleTerm',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Role = {
	title : 'role',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ ],
	elements : [ RoleTerm ]
};

var Description = {
	title : 'description',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Name = {
	title : 'name',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ ID_attr, xlink_attr, xmllang_attr, script_attr, transliteration_attr, name_type_attr, authority_attr, authorityURI_attr, valueURI_attr, displayLabel_attr, usage_attr, altRepGroup_attr, nameTitleGroup_attr ],
	elements : [ NamePart, DisplayForm, Affiliation, Role, Description ]
};

var TypeOfResource = {
	title : 'typeOfResource',
	repeatable : true,
	type : 'selection',
	singleton : false,
	values : ['','text', 'cartographic', 'notated music', 'sound recording-musical', 'sound recording-nonmusical', 'sound recording', 'still image', 'moving image', 'three dimensional object', 'software', 'multimedia mixed material'],
        attributes : [ collection_attr, manuscript_attr, displayLabel_attr, usage_attr, altRepGroup_attr ]
};


var Genre = {
	title : 'genre',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, genre_authority_attr, authorityURI_attr, valueURI_attr, genre_type_attr, displayLabel_attr, usage_attr, altRepGroup_attr],
	elements : [ ]
};

var PlaceTerm = {
	title : 'placeTerm',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ placeTerm_type_attr, placeTerm_authority_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Place = {
	title : 'place',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ supplied_attr ],
	elements : [ PlaceTerm ]
};

var Publisher = {
	title : 'publisher',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ supplied_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};


var DateIssued = {
	title : 'dateIssued',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, keyDate_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var DateCreated = {
	title : 'dateCreated',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, keyDate_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var DateCaptured = {
	title : 'dateCaptured',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, keyDate_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var DateValid = {
	title : 'dateValid',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, keyDate_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var DateModified = {
	title : 'dateModified',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, keyDate_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};


var CopyrightDate = {
	title : 'copyrightDate',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, keyDate_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};


var DateOther = {
	title : 'dateOther',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, keyDate_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};


var Edition = {
	title : 'edition',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ supplied_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};


var Issuance = {
	title : 'issuance',
	repeatable : true,
	type : 'selection',
	singleton : false,
	values: ['continuing', 'monographic', 'single unit', 'multipart monograph', 'serial', 'integrating resource'],
        attributes : [ ],
	elements : [ ]
};


var Frequency = {
	title : 'frequency',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ frequency_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var OriginInfo = {
	title : 'originInfo',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, altRepGroup_attr ],
	elements : [ Place, Publisher, DateIssued, DateCreated, DateCaptured, DateValid, DateModified, CopyrightDate, DateOther, Edition, Issuance, Frequency ]
};


var LanguageTerm = {
	title : 'languageTerm',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ languageTerm_type_attr, languageTerm_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var ScriptTerm = {
	title : 'scriptTerm',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ scriptTerm_type_attr, scriptTerm_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};


var Language = {
	title : 'language',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ objectPart_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, usage_attr, altRepGroup_attr ],
	elements : [ LanguageTerm, ScriptTerm ]
};

var Form = {
	title : 'form',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ form_authority_attr, authorityURI_attr, valueURI_attr, form_type_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var ReformattingQuality = {
	title : 'reformattingQuality',
	repeatable : true,
	type : 'selection',
	singleton : false,
	values : [ '', 'access', 'preservation', 'replacement'],
        attributes : [ ],
	elements : [ ]
};

var InternetMediaType = {
	title : 'internetMediaType',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Extent = {
	title : 'extent',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ supplied_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var DigitalOrigin = {
	title : 'digitalOrigin',
	repeatable : true,
	type : 'selection',
	singleton : false,
	values : ['', 'born digital', 'reformatted digital', 'digitized microfilm', 'digitized other analog'],
        attributes : [ ],
	elements : [ ]
};

var Physical_Description_Note = {
	title : 'note',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, note_type_attr, ID_attr ],
	elements : [ ]
};

var PhysicalDescription = {
	title : 'physicalDescription',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, altRepGroup_attr ],
	elements : [ Form, ReformattingQuality, InternetMediaType, Extent, DigitalOrigin, Physical_Description_Note ]
};

var Abstract = {
	title : 'abstract',
	repeatable : true,
	type : 'textarea',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, abstract_type_attr, shareable_attr, altRepGroup_attr ],
	elements : [ ]
};
var TableOfContents = {
	title : 'tableOfContents',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, tableOfContents_type_attr, shareable_attr, altRepGroup_attr ],
	elements : [ ]
};
var TargetAudience = {
	title : 'targetAudience',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, targetAudience_authority_attr, authorityURI_attr, valueURI_attr, displayLabel_attr, altRepGroup_attr ],
	elements : [ ]
};
var Note = {
	title : 'note',
	repeatable : true,
	type : 'textarea',
	singleton : false,
        attributes : [ ID_attr, xlink_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, primary_note_type_attr, altRepGroup_attr ],
	elements : [ ]
};

var Occupation = {
	title : 'occupation',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ occupation_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Coordinates = {
	title : 'coordinates',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Projection = {
	title : 'projection',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Scale = {
	title : 'scale',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Cartographics = {
	title : 'cartographics',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ ],
	elements : [ Scale, Projection, Coordinates ]
};

var CitySection = {
	title : 'citySection',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var ExtraterrestrialArea = {
	title : 'extraterrestrialArea',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Area = {
	title : 'area',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Island = {
	title : 'island',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var City = {
	title : 'city',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var County = {
	title : 'county',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Territory = {
	title : 'territory',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var State = {
	title : 'state',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Region = {
	title : 'region',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Province = {
	title : 'province',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Country = {
	title : 'country',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Continent = {
	title : 'continent',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var HierarchicalGeographic = {
	title : 'hierarchicalGeographic',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ hierarchicalGeographic_authority_attr, authorityURI_attr, valueURI_attr ],
	elements : [ Continent, Country, Province, Region, State, Territory, County, City, Island, Area, ExtraterrestrialArea, CitySection ]
};

var Temporal = {
	title : 'temporal',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ temporal_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr, temporal_encoding_attr, point_attr, keyDate_attr, temporal_qualifier_attr ],
	elements : [ ]
};

var Geographic = {
	title : 'geographic',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ geographic_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var GeographicCode = {
	title : 'geographicCode',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ geographicCode_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};


var Topic = {
	title : 'topic',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ topic_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var Subject = {
	title : 'subject',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ ID_attr, xlink_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr, subject_authority_attr, authorityURI_attr, valueURI_attr, displayLabel_attr, usage_attr, altRepGroup_attr ],
	elements : [ Topic, Geographic, Temporal, TitleInfo, Name, GeographicCode, Genre, HierarchicalGeographic, Cartographics, Occupation ]
};

var Classification = {
	title : 'classification',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, classification_authority_attr, authorityURI_attr, valueURI_attr, edition_attr, displayLabel_attr, usage_attr, altRepGroup_attr ],
	elements : [ ]
};

var Identifier = {
	title : 'identifier',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, identifier_type_attr, displayLabel_attr, invalid_attr, altRepGroup_attr ],
	elements : [ ]
};

var HoldingExternal = {
	title : 'holdingExternal',
	repeatable : false,
	type : 'textarea',
	singleton : false,
        attributes : [ ],
	elements : [ ]
};

var EnumerationAndChronology = {
	title : 'enumerationAndChronology',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ unitType_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var CopyInformation_Note = {
	title : 'note',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ ID_attr, xlink_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, ci_note_type_attr ],
	elements : [ ]
};

var ElectronicLocator = {
	title : 'electronicLocator',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ ],
	elements : [ ]
};

var ShelfLocator = {
	title : 'shelfLocator',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var SubLocation = {
	title : 'subLocation',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var CopyInformation_Form = {
	title : 'form',
	repeatable : false,
	type : 'text',
	singleton : false,
        attributes : [ ci_form_authority_attr, ci_form_type_attr, authorityURI_attr, valueURI_attr, ID_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var CopyInformation = {
	title : 'copyInformation',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ ],
	elements : [ CopyInformation_Form, SubLocation, ShelfLocator, ElectronicLocator,  CopyInformation_Note, EnumerationAndChronology ]
};

var HoldingSimple = {
	title : 'holdingSimple',
	repeatable : false,
	type : 'text',
	singleton : false,
        attributes : [ ],
	elements : [ CopyInformation ]
};

var Url = {
	title : 'url',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ dateLastAccessed_attr, displayLabel_attr, url_note_attr, url_access_attr, url_usage_attr ],
	elements : [ ]
};

var PhysicalLocation = {
	title : 'physicalLocation',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ physicalLocation_authority_attr, authorityURI_attr, valueURI_attr, displayLabel_attr, physicalLocation_type_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};


var Location = {
	title : 'location',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, altRepGroup_attr ],
	elements : [ PhysicalLocation, ShelfLocator, Url, HoldingSimple, HoldingExternal ]
};

var AccessCondition = {
	title : 'accessCondition',
	repeatable : true,
	type : 'textarea',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, accessCondition_type_attr, altRepGroup_attr ],
	elements : [ ]
};
var Part_Text = {
	title : 'text',
	repeatable : true,
	type : 'textarea',
	singleton : false,
        attributes : [ xlink_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr, part_text_type_attr ],
	elements : [ ]
};
var Part_Date = {
	title : 'date',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};
var List = {
	title : 'list',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};
var Total = {
	title : 'total',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ ],
	elements : [ ]
};
var End = {
	title : 'end',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};
var Start = {
	title : 'start',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};
var Extent = {
	title : 'extent',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ unit_attr ],
	elements : [ Start, End, Total, List ]
};
var Detail_Title = {
	title : 'title',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ detail_title_type_attr, level_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};
var Caption = {
	title : 'caption',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};
var Detail_Number = {
	title : 'number',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};
var Detail = {
	title : 'detail',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ ],
	elements : [ Detail_Number, Caption, Title ]
};

var Part = {
	title : 'part',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ ID_attr, part_type_attr, order_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, altRepGroup_attr ],
	elements : [ Detail, Extent, Part_Date, Part_Text ]
};

var Extension = {
	title : 'extension',
	repeatable : true,
	type : 'textarea',
	singleton : false,
        attributes : [ displayLabel_attr ],
	elements : [ ]
};

var DescriptionStandard = {
	title : 'descriptionStandard',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ descriptionStandard_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var LanguageOfCataloging = {
	title : 'languageOfCataloging',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ objectPart_attr, altRepGroup_attr, usage_attr, displayLabel_attr ],
	elements : [ LanguageTerm, ScriptTerm ]
};

var RecordOrigin = {
	title : 'recordOrigin',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var RecordIdentifier = {
	title : 'recordIdentifier',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var RecordChangeDate = {
	title : 'recordChangeDate',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, keyDate_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var RecordCreationDate = {
	title : 'recordCreationDate',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ encoding_attr, point_attr, keyDate_attr, qualifier_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var RecordContentSource = {
	title : 'recordContentSource',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ recordContentSource_authority_attr, authorityURI_attr, valueURI_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};

var RecordInfo = {
	title : 'recordInfo',
	repeatable : true,
	type : 'none',
	singleton : true,
        attributes : [ ID_attr, xlink_attr, displayLabel_attr, relatedItem_type_attr ],
	elements : [ RecordContentSource, RecordCreationDate, RecordChangeDate, RecordIdentifier, RecordOrigin, LanguageOfCataloging, DescriptionStandard ]
};

var RelatedItem = {
	title : 'relatedItem',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ ID_attr, xlink_attr, displayLabel_attr, relatedItem_type_attr ],
	elements : [ TitleInfo, Name, TypeOfResource, Genre, OriginInfo, Language, PhysicalDescription, Abstract, TableOfContents, TargetAudience, Note, Subject, Classification, Identifier, Location, AccessCondition, Part, Extension, RecordInfo ]
};


/*
var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
	elements : [ ]
};
*/
</script>
</body>
