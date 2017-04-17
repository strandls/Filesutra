var filesutraControllers = angular.module("filesutraControllers", ["filesutraServices"]);

filesutraControllers.controller("AppCtrl", ['$scope', '$http', '$location', "fileService", "authService",
        function($scope, $http, $location, fileService, authService) {
            $scope.toggleObject = true;
            $scope.importing = false;
            $scope.loadMoreText = "Load More"

                $scope.selectApp = function(app) {
                    $scope.runningApp = app;
                    $location.path(app);
                }

            $scope.login = function(app) {
                var redirectUrl = '/auth/' + (app == 'AmazonCloudDrive'? 'amazon' : app.toLowerCase());
                if (window.opener) {
                    window.location = redirectUrl;
                } else {
                    var oAuthWndow = window.open(redirectUrl, "Filesutra", "width=800, height=600, top=100, left=300");
                    var interval = window.setInterval(function() {
                        console.log('sdfsdfdf');
                        if (oAuthWndow.location.href.indexOf('picker') != -1) {
                            oAuthWndow.close();
                            location.reload();
                        }
                    }, 1000);
                }
            }

            $scope.logout = function(app) {
                var connectedAppPos = $scope.appSettings.connectedApps.indexOf(app)
                    if (connectedAppPos != -1) {
                        authService.logout(app, function(data) {
                            if (data.success) {
                                $scope.appSettings.connectedApps.splice(connectedAppPos, 1);
                                $location.path(app);
                            }
                        });
                    }
            }

            $scope.isConnected = function(app) {
                if ($scope.appSettings.connectedApps.indexOf(app) != -1) {
                    return true;
                } else {
                    return false;
                }
            }
            $scope.setProgress = function(percentVal){
               
                //$('#submitIt').submit();
                var me1 = $('#submitIt');
                var bar = $('.progress-bar');
                var percent = $('.percent');
                var progress = $('.progress');
                var status = $('#status');
                percentVal = Math.floor(percentVal)+'%';
                bar.width(percentVal);
                percent.html(percentVal);
                bar.css("width", percentVal).text(percentVal);
                if(percentVal == '0%' || percentVal == '100%') {
                    progress.hide();    
                } else {
                    progress.show(); 
                }
            }

            $scope.uploadFileSelect = function(event){
                $('#importBtn').prop('disabled', false);
                $('#importBtn').removeAttr('disabled');

                $scope.filesSelected = true;
                return false;
            }

            $scope.uploadFile = function(event){
                //$('#submitIt').submit();
                var me1 = $('#submitIt');
                var bar = $('.progress-bar');
                var percent = $('.percent');
                var status = $('#status');

                $scope.filesSelected = false;

                me1.ajaxSubmit({
                        url : "/demo/upload",
                        dataType : "json",
                        beforeSend: function() {
                            $('#status').empty();
                            $scope.setProgress(0);
                        },
                        uploadProgress: function(event, position, total, percentComplete) {
                            var percentVal = percentComplete;
                            $scope.setProgress(percentVal);
                        },
                        complete: function(xhr) {
                            var str = '<ul style="text-align:left;">';
                            xhr.responseText = JSON.parse(xhr.responseText);
                            for(var i=0; i<xhr.responseText.length; i++) {
                                console.log(xhr.responseText[i]);
                                str += "<li>"+xhr.responseText[i].originalFilename+" ("+xhr.responseText[i].contentType+" - "+xhr.responseText[i].size+")</li>";
                            }
                            str += "</ul>";
                            status.html(str);
                        },
                        success : function (response, statusText, xhr, form) {
                            var images = response;
                            //console.log(typeof(images))
                            //console.log(images)
                            var message = {
                                type  : 'filesutra',
                                data   :  images
                            }
                            if (window.opener) {
                                window.opener.postMessage(message, '*');
                                window.close();
                            } else {
                                // iframe
                                parent.postMessage(message, '*');
                            }
                            $scope.importing = false;
                            $('#importBtn').prop('disabled', false);
                            $('#importBtn').removeAttr('disabled');

                            $(event.currentTarget).removeClass('active').text('Import');
                        },
                        error : function(xhr, ajaxOptions, thrownError){
                            console.log(thrownError);
                        }
                    });
            }

            $scope.uploadVideoFile = function(event){
                var me1 = $('#submitVideo')
                    me1.ajaxSubmit({
                        url : "/demo/uploadVideo",
                        dataType : "json",

                        success : function (response, statusText, xhr, form) {
                            var images = response.videos;
                            console.log(images);
                            //console.log(typeof(images))
                            //console.log(images)
                            var message = {
                                type  : 'filesutra',
                                contentType : 'videoUrl',
                                data   :  images
                            }
                            if (window.opener) {
                                window.opener.postMessage(message, '*');
                                window.close();
                            } else {
                                // iframe
                                parent.postMessage(message, '*');
                            }
                        },
                        error : function(xhr, ajaxOptions, thrownError){
                            console.log(thrownError);
                        }
                    });
            };


            $scope.selectItem = function (item) {
                if($scope.importing && item.type != "folder") {
                    return;
                }
                $('#status').empty();
                $scope.setProgress(0);

                if (item.type == "folder") {
                    $location.path($location.path()+'/'+item.id);
                }else{
                    var addToArray = true;
                    for(var i=0;i<$scope.userGroupId.length;i++){
                        if($scope.userGroupId[i]['id'] == item.id){
                            var index = i;//$scope.userGroupId.indexOf(item.id);
                            if (index > -1) {
                                $scope.userGroupId.splice(index, 1);
                                $scope.itemId.splice(index, 1);
                            }
                            addToArray = false;
                        }
                    }
                    if(addToArray){
                        $scope.itemId.push(item.id);
                        $scope.userGroupId.push(item);
                        console.log($scope.itemId);
                    }

                    if($scope.itemId.length > 0) {
                        $('#importBtn').prop('disabled', false);
                        $('#importBtn').removeAttr('disabled');
                    } else {
                        $('#importBtn').prop('disabled', 'disabled');
                        $('#importBtn').attr('disabled', 'disabled');
                    }
                }

                $scope.selectedItem = item;
            }

            $scope.import = function(e) {
                console.log($(e));
                $scope.importing = true;
                $(e.currentTarget).text('Importing');
                $('#importBtn').prop('disabled', 'disabled');
                $('#importBtn').attr('disabled', 'disabled');

                if($scope.app == 'Local') {
                    $scope.uploadFile(e);
                    return;
                }

                var uploadCount = 0;
                var importedFiles = [];
                $('#status').empty();
                $scope.setProgress(0);
                for(var i=0;i<$scope.userGroupId.length;i++){
                    fileService.import($scope.app, $scope.userGroupId[i], function(data) {
                        //console.log(data);
                        uploadCount++;
                        importedFiles.push(data);
                        if(uploadCount == $scope.userGroupId.length ){
                            console.log(importedFiles);
                            var message = {
                                type  : 'filesutra',
                                data   :  importedFiles
                            }
                            if (window.opener) {
                                window.opener.postMessage(message, '*');
                                window.close();
                            } else {
                                // iframe
                                parent.postMessage(message, '*');
                            }
                            var percentVal = ((uploadCount/$scope.userGroupId.length)*100);
                            $scope.setProgress(percentVal);
                            $scope.importing = false;
                            $('#importBtn').prop('disabled', false);
                            $('#importBtn').removeAttr('disabled');

                            $(e.currentTarget).text('Import');
                            $('.selectedItem').removeClass('selectedClass');
                            $scope.selectedItem = undefined;
                            $scope.itemId.length = 0;
                            $scope.userGroupId.length = 0;
                        } else {
                            var percentVal = ((uploadCount/$scope.userGroupId.length)*100);
                            $scope.setProgress(percentVal);
                        }
                    });
                }
            }


            $scope.init = function(appSettings){
                $scope.appSettings = appSettings;
                $.ajaxSetup({cache:false});
                $(document).on("ajaxStart", function(){
                    console.log('ajaxStart----------------------------');
                    $('.container').addClass('busy');
                }).on("ajaxStop", function(){
                    console.log('ajaxStop----------------------------');
                    $('.container').removeClass('busy');
                });
                $scope.filesSelected = false;
            }
            $scope.backButton = function(){
                window.history.back();
            }

            $scope.$on("$locationChangeSuccess", function (event, newUrl) {
                console.log(event);
                $scope.gettingList(0);
                $scope.setProgress(0);
                $('#status').empty();
                $scope.filesSelected = false;
                $('#importBtn').prop('disabled', 'disabled');
                $('#importBtn').attr('disabled', 'disabled');
                console.log($location.search('resType'));
            });

            $scope.gettingList = function(code){
                $scope.showBackButton = false;
                var path = $location.path();
                var chunks = path.split("/");
                var app, folderId;
                console.log(chunks);
                if (chunks.length < 2) {
                    $scope.selectApp("Local");
                    return;
                } else {
                    app = chunks[1];
                    $scope.app = app;
                    $scope.runningApp = app;
                }
                if (chunks.length > 2) {
                    folderId = chunks[chunks.length - 1];
                }
                console.log($scope.app);
//                if($scope.app == "Facebook" || $scope.app == "Flickr"){

                    if(code==0){
                        $scope.showButton = false;
                        delete $scope.items;
                        $scope.afterTokenVal = '';

                        if ($scope.isConnected(app)) {
                            $scope.userGroupId = [];
                            $scope.itemId = [];
                            fileService.getItems(app, folderId, $scope.afterTokenVal, function (items) {
                                if (chunks.length > 2) {
                                    $scope.showBackButton = true;
                                }else{
                                    $scope.showBackButton = false;
                                }
                                $scope.items = [];
                                $scope.afterTokenVal = items.afterval;
                                if(items.itemResponse.length < 25){
                                    $scope.showButton = false;
                                }else{
                                    $scope.showButton = true;
                                }
                                console.log( $scope.showButton);
                                for(var i=0; i< items.itemResponse.length;i++){
                                    $scope.items.push(items.itemResponse[i]);
                                }
                            });
                        }
                    }else{
                        $scope.loadMoreText = "Loading..."
                            $scope.isDisabled = true;
                        fileService.getItems(app, folderId, $scope.afterTokenVal, function (items) {
                            $scope.loadMoreText = "Load More"
                                $scope.afterTokenVal = items.afterval;
                            if (chunks.length > 2) {
                                $scope.showBackButton = true;
                            }else{
                                $scope.showBackButton = false;
                            }
                            console.log(items);
                            if(items!="error"){
                                if(items.itemResponse.length < 25){
                                    $scope.showButton = false;
                                }else{
                                    $scope.showButton = true;
                                    $scope.isDisabled = false;
                                }
                                for(var i=0; i< items.itemResponse.length;i++){
                                    $scope.items.push(items.itemResponse[i]);
                                }
                            }else{
                                $scope.showButton = false;
                            }
                        });
                    }
               /* } else {
                    console.log('else');
                    $scope.showButton = false;
                    if ($scope.isConnected(app)) {
                        console.log('isConnected');
                        $scope.userGroupId = [];
                        $scope.itemId = [];
                        delete $scope.items;
                        $scope.items = null;
                        fileService.getListItems(app, folderId, function (items) {
                            console.log(items);
                            if (chunks.length > 2) {
                                $scope.showBackButton = true;
                            }else{
                                $scope.showBackButton = false;
                            }
                            $scope.items = items.itemResponse;
                        });
                    }
                }*/

            }
            $scope.wikiLogin = function(app) {
                 $("#wikiLoginMsg").html("").hide();
                 $("#wikiFormLogin").submit(function( event ) {
                     var username = $('input#wikiUser').val();
                     var password = $('input#wikiPassword').val();
                     if(username && password) {
                         return;
                     } else {
                        $("#wikiLoginMsg").html("Username and password cannot be empty").show();
                        event.preventDefault();
                     }
                });
            }
        }]);
