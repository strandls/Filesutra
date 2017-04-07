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

  <link rel="stylesheet" href="/css/bootstrap.min.css">

  <script src="/js/lib/jquery.min.js"></script>
  <script src="/js/lib/angular.min.js"></script>
  <script src="/js/lib/angular-route.min.js"></script>
  <script src="/js/app.js"></script>
  <script src="/js/controllers.js"></script>
  <script src="/js/services.js"></script>
  <script src="/js/jquery.form.js"></script>
  <!--script src="http://filesutra.com/js/filesutra.js"></script-->

  <style>
  .container {
    width:100%;
      overflow-x:hidden;
  }
    li a {
      cursor: pointer;
    }
    .filesPane {
/*      min-height: 400px;
      max-height: 400px;
      overflow: auto;
*/    }
    .selectedItem {
      background-color: #46b8da;
    }
    .filesutraItem {
      cursor: pointer;
    margin:0px;
    }
    .filesutraItem a{
        height:50px;
        margin:3px;
        text-overflow: ellipsis;
        display: inline-block;
        /* width: 150px; */
        white-space: nowrap;
        overflow: hidden;
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
    .import-btn {
      margin-right: 20px;
    }
    body.busy, body.busy * {  
    cursor: wait !important;  
    }
    .spinner {
    opacity: 0;
    display:none;
    width: 20px;
    height:10px;
        -webkit-transition: opacity 0.25s, max-width 0.45s;
        -moz-transition: opacity 0.25s, max-width 0.45s;
        -o-transition: opacity 0.25s, max-width 0.45s;
    transition: opacity 0.25s, max-width 0.45s;
                /* Duration fixed since we animate additional hidden width */
    }
    .has-spinner.active {
    cursor:progress;
    }
    .has-spinner.active .spinner {
    display:inline-block;
    opacity: 1;
            max-width: 50px;
            /* More than it will ever come, notice that this affects on animation duration */
    }

.snippet {
	overflow: hidden;
	position: relative;
	border-radius: 5px;
}

.thumbnail {
    margin:0 5px 5px 0px;
    padding:0px;
    background:#ccc;
}

.snippet.tablet {
	width: 150px;
	height: 195px;
}

.snippet.tablet .figure {
	height:150px;
	width:auto;
}
.snippet.tablet .caption {
	height:25px;
        padding:0px 0px;
        position:relative;
         text-overflow: ellipsis;
        display: inline-block;
        white-space: nowrap;
        overflow: hidden;

}
.snippet.tablet .caption .story-footer {
        left:0px;
}

.thumbnail .figure { 
	margin-left: auto;
	margin-right: auto;
	display: table;
	table-layout:fixed;
}

.thumbnail .figure a, .thumbnail .figure span {
	display: table-cell;
	vertical-align: middle;
	max-height: 150px;
	max-width: 150px;
}

.img-polaroid {
    border:none;
}

.thumbnail .figure .img-polaroid {
    padding:0px;
	height:150px; 
 	width: 150px; 
	background-repeat:no-repeat; 
	background-position:center;
    justify-content: center;
    /* margin: 0 auto; */
    object-fit: contain;
    vertical-align: middle;
}

.thumbnail .figure .mouseover, .thumbnail .figure .mouseoverfix {
    padding-left: 0px;
    position: absolute;
    left: 0;
    bottom: 0;
    right: 0;
    background-color: whitesmoke;
    opacity: 0.8;
    height: 100%;
    text-align: justify;
    width: 100%;
}


.thumbnail {
	/*background-color: #ffffff;
	border: 0px solid #bee6d3;
	*/position:relative; 
    float:left;
}

.thumbnail:hover {
	background-color: #fefad5;
}
img.loading {
        background: transparent url(spinner.gif) no-repeat scroll center center;
}
  </style>
</head>

<body>

<div class="container submitObs" style="padding: 10px;">
  <div class="row" ng-controller="AppCtrl" ng-init="init(${appSettings})">
    <div id="fileSource" class="col-md-3 col-sm-3" style="padding:0px;position:fixed;">
    <ul class="list-group">
    <li class="list-group-item" ng-click="selectApp('Local')">
    <a >My Computer</a>
    </li>
    <li class="list-group-item"  ng-click="selectApp('Facebook')">
    <a>Facebook</a>
    <a ng-if="isConnected('Facebook')" ng-click="logout('Facebook')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>
    <li class="list-group-item" ng-click="selectApp('Google')">
    <a >Google Drive</a>
    <a ng-if="isConnected('Google')" ng-click="logout('Google')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>

    <li class="list-group-item" ng-click="selectApp('Photos')">
    <a>Google Photos</a>
    <a ng-if="isConnected('Photos')" ng-click="logout('Photos')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>

    <li class="list-group-item" ng-click="selectApp('Flickr')">
    <a >Flickr</a>
    <a ng-if="isConnected('Flickr')" ng-click="logout('Flickr')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>
    <li class="list-group-item" ng-click="selectApp('Dropbox')">
    <a>Dropbox</a>
    <a ng-if="isConnected('Dropbox')" ng-click="logout('Dropbox')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>
    <li class="list-group-item" ng-click="selectApp('Wikimedia')">
    <a>Wikimedia</a>
    <a ng-if="isConnected('Wikimedia')" ng-click="logout('Wikimedia')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>
    <li class="list-group-item" ng-click="selectApp('Youtube')">
    <a >Youtube</a>
    </li>
 
    <!--li class="list-group-item">
          <a ng-click="selectApp('Box')">Box</a>
          <a ng-if="isConnected('Box')" ng-click="logout('Box')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
        </li>
        <li class="list-group-item">
          <a ng-click="selectApp('OneDrive')">OneDrive</a>
          <a ng-if="isConnected('OneDrive')" ng-click="logout('OneDrive')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
        </li>
        <li class="list-group-item">
          <a ng-click="selectApp('AmazonCloudDrive')">Amazon Cloud Drive</a>
          <a ng-if="isConnected('AmazonCloudDrive')" ng-click="logout('AmazonCloudDrive')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
        </li-->
              </ul>
    </div>
    <div class="col-md-9 col-sm-9" style="margin-left:26%; width:73%;">
    <div class="row filesPane">
        <div>
          <div ng-if="app!=undefined ">
            <div ng-if="!isConnected(app) && runningApp !='Local' && runningApp !='Youtube'" style="text-align: center; margin-top: 40px">
              <a class="btn btn-primary" ng-click="login(app)">
                Connect {{app=='AmazonCloudDrive'? 'Amazon Cloud Drive' : app}}</a>
            </div>
             <div ng-if="!isConnected(app) && runningApp =='Local'" style="text-align: center; margin-top: 40px">
              <form id="submitIt" class="upload_resource1" method="post"  enctype="multipart/form-data" style="text-align: -moz-center;">                
                <input type="file" class="fileUploadInput btn btn-primary" style="display: -webkit-inline-box;" name="resources" id="fileUploadInput" custom-on-change="uploadFile" accept="image/*|audio/*" title="Choose File" multiple/> 
              </form>                
            </div>
             <div ng-if="!isConnected(app) && runningApp =='Youtube'" style="text-align: center; margin-top: 40px">
              <form id="submitVideo" class="form-horizontal upload_resource2" method="post" style="text-align: -moz-center;">                
              <div class="form-group">
              <label  class="col-sm-3 control-label">Enter YouTube watch url like http://www.youtube.com/watch?v=v8HVWDrGr6o</label>
              <div class="col-sm-9">
              <input type="text" class="fileUploadInput form-control" name="videoResources" id="videoFileUploadInput" custom-on-change="uploadVideoFile" title="Please enter youtube video url" placeholder="Add Video">
              </div>
              </div>
              <div class="form-group">
              <div class="col-sm-offset-2 col-sm-10">
              <button type="submit" class="btn btn-default">Submit</button>
              </div>
              </div>
              </form>                
            </div>


            <div ng-if="isConnected(app)">

            <div class="" style="border-bottom:1px solid #cfcfcf; text-align:center;margin-bottom:10px;">
            <a class="btn btn-sm btn-primary pull-left glyphicon glyphicon-chevron-left" ng-if="showBackButton" ng-click="backButton()" style=""></a>
            <h4>{{app}}</h4>
            </div>
            <div class="thumbnails clearfix" style="height:227px;overflow-y:auto;">
              <div ng-if="!items" style="text-align: center;">
                Loading...
              </div>
              <div ng-if="items.length == 0" style="text-align: center;">
                No Files or Folders
              </div>
                <div class="list-group filesutraItem"  >
                    <a class="col-sm-2 list-group-item" ng-class-odd="{'color1':toggleObject}" ng-repeat="item in items"  ng-if="item.type != 'file'" ng-click="selectItem(item); toggleObject = !toggleObject"
                       ng-class="itemId.indexOf(item.id) > -1 && item.type != 'folder' ? 'selectedItem' : ''">
                     <i ng-class="item.type == 'file' ? 'glyphicon glyphicon-file' : 'glyphicon glyphicon-folder-open'"></i>
                      {{item.name}}

                      </a>
                  </div>
                  <div class="imgContainer thumbnail" ng-if="item.type == 'file'"  ng-repeat="item in items">
                    
                      <ul ng-click="selectItem(item); toggleObject = !toggleObject" style="padding:0px;margin:0px;list-style:none;">
                        <li class="snippet tablet" ng-class="itemId.indexOf(item.id) > -1 && item.type != 'folder' ? 'selectedItem' : ''" style="height:100%">
                        <div class="figure">
                          <img  ng-if="runningApp != 'Google'" src="{{item.iconurl}}" class="img-responsive img-polaroid loading"/>
                          <img  ng-if="runningApp == 'Google'" src="{{item.thumbnail}}" class="img-responsive img-polaroid loading"/>
                          </div>
                        <div class="caption">
                        {{item.name}}
                        </div>
                       </li>
                      </ul>
                        <button id="singlebutton" name="singlebutton" ng-disabled="isDisabled" style="margin:0 auto;display:block;" class="btn btn-primary bottom2" ng-show="$last && showButton" ng-click="gettingList(1)">{{loadMoreText}}</button> 
                  </div>
                  </div>
         
        <div style="clear:both; border-top:1px solid #cfcfcf;padding-top:5px;margin-top:10px;">
        <a class="btn btn-primary pull-right import-btn has-spinner" ng-if="app && isConnected(app)" style="text-align:center;" ng-disabled="itemId.length == 0 || selectedItem.type == 'folder'" ng-click="import($event)"> <span class="spinner"><asset:image src="/images/spinner.gif" absolute="true"/></span> Import</a>
        </div>
    
                  
            </div>
          </div>
          
        </div>
             </div>
    </div>
  </div>
</div>
</body>
</html>
