<%--
  Created by IntelliJ IDEA.
  User: vishesh
  Date: 05/05/15
  Time: 7:31 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html ng-app="filesutraApp">
<head>
  <title>File Sutra</title>
  <link rel="shortcut icon" href="/images/favicon.ico" />

  <asset:stylesheet href="application.css"/>
<style>
    li a {
      cursor: pointer;
    }
    .filesPane {
      min-height: 400px;
      max-height: 400px;
      overflow: auto;
    }
    .selectedItem {
      background-color: #46b8da;
    }
    .color1{
      background-color: #edf3fe;
    }
    .filesutraItem {
      cursor: pointer;
    }
    .action-group{
      width: 100%;
      position: absolute;
      bottom: 90px;
      margin-left: 30px;
    }
    .list-group-item1{
      position: relative;
      height: 150px;
      width: 150px;
      margin: 0.5cm;
      display: block;
      padding: 10px 15px;
      text-align: center;
      /*border: 1px solid #ddd;*/
    }
    .bottom2 { 
      width: 250px;   
    }
    .imgContainer{
    float:left;
    }
    .import-btn {
      margin-right: 20px;
    }
  </style>
</head>

<body>

<div class="container submitObs" style="padding: 10px">
  <div class="row" ng-controller="AppCtrl" ng-init="init(${appSettings})">

        <g:if test="${flash.message}"> 
        <div class="alert alert-danger">${flash.message}</div>
        </g:if>
      <div class="row">
        <form id="wikiLoginForm" action='wikimediaCallback' method='POST' class="form-horizontal" name='loginForm'>  
        
        <div class="control-group">         
        <label class="control-label" for="username">
        Wikimedia username
        </label>
        <div class="controls">
        <input id="wikiUser" class="input-xlarge focused" type="text" name="username">
        </div>         
        </div>             
        <div class="control-group" style="clear: both;">                                                                                          
        <label class="control-label" for="password">Password
        </label>
        <div class="controls">
        <input id="wikiPassword" class="input-xlarge" type="password" name="password">
        </div>
        </div>             

        <div class="control-group" style="clear: both;">
        <span id="wikiLoginMsg" class="alert alert-error" style="display:none"></span>
        <div class="controls">
        <button type="submit" class="btn btn-primary import-btn" ng-if="app"
        ng-click="wikiLogin(app)">Login</button>
        </div>
        </div>
        
        </form>

      </div>
  </div>
</div>

  <asset:javascript src="application.js"/>
</body>
</html>
