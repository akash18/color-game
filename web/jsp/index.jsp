<%@ include file="header.jsp"%>

</head>
<body>
    <div id="countdown_box">
        <div id="countdown_div">&nbsp;</div>
        <div id="countdown_msg">Please wait for few seconds till you get unlocked..</div>
    </div>
    <div id="output"></div>

    <div class="container-fluid login">
        <div class="row">
            <div class="col-md-4">&nbsp;</div>
            <div class="col-md-4" style="color: white;">
                <h1>Color the Block</h1>
            </div>
            <div class="col-md-4">&nbsp;</div>
        </div>
        <div class="row">&nbsp;</div>
        <div class="row">&nbsp;</div>
        <div class="row">&nbsp;</div>
        <div class="row">&nbsp;</div>
        <div class="row">&nbsp;</div>
        <div class="row">
            <div class="col-md-4 center-block">&nbsp;</div>
            <div class="col-md-4 center-block">
                <button type="button" data-id="join" class="btn btn-warning btn-lg gameBtn"
                        data-toggle="modal" data-target="#playerInfoPopup" onclick="setButtonId(this); return true;">Join Game !!!</button>&nbsp;&nbsp;
                <button type="button" data-id="start" class="btn btn-warning btn-lg gameBtn"
                        data-toggle="modal" data-target="#playerInfoPopup" onclick="setButtonId(this); return true;">Start Game !!!</button>
            </div>
            <div class="col-md-4 center-block">&nbsp;</div>
        </div>
        <br />
    </div>

    <div class="container-fluid game">
        <div class="row">
            <div class="col-md-4 center-block">&nbsp;</div>
            <div class="col-md-4 center-block">
                <h1 style="color: white;">Color Me !!!! Hurry....</h1>
            </div>
            <div class="col-md-4 center-block">&nbsp;</div>
        </div>
        <div class="row">&nbsp;</div>
        <div class="row">&nbsp;</div>
        <div class="row">&nbsp;</div>
        <div class="row">&nbsp;</div>
        <div class="row">

            <div class="col-md-6 center-block">
                <table class="table table-bordered" id="myTable">
                    <tbody>
                        <c:forEach var="i" begin="1" end="${rows}">
                            <tr>
                                <c:forEach var="j" begin="1" end="${columns}">
                                    <td id="${i}${j}" onclick="colorBlock(this);" class="hoverEffect">&nbsp;</td>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="container" id="scoreContainer">
                <div class="row">
                    <div class="col-md-6">
                        <div class="panel panel-primary">

                            <div class="panel-heading">
                                <h3 class="panel-title">Scoreboard</h3>    
                            </div>
                            <table class="table table-hover header-fixed" id="scoreboard">
                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Player Name</th>
                                        <th>Color</th>
                                        <th>Score</th>
                                    </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

        </div>
        <!--        <div class="col-md-4 center-block">&nbsp;</div>-->

    </div>

    <!-- Modal -->
    <div id="playerInfoPopup" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Player Information</h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label style="color: Black;">Name:</label> 
                        <input name="name" id="userName" class="form-control" required="required" placeholder="Enter name" style="width: 30%;" />
                        <input type="hidden" name="flag" id="flag" class="form-control" style="width: 30%;" />
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-warning" data-dismiss="modal">No
                        Thanks</button>
                    <button type="button" class="btn btn-success" data-dismiss="modal"
                            onclick="startGame(${rows},${columns}); return true;">Start</button>
                </div>
            </div>

        </div>
    </div>

    <div id="resultPopup" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Game Over!!!</h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label style="color: Black;" id="msg"></label> 
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-warning" data-dismiss="modal">Ok</button>
                </div>
            </div>

        </div>
    </div>

    <div id="gameInfoPopup" data-backdrop="static" data-keyboard="false" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Game Information</h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label style="color: Black;" id="msg"></label> 
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-warning" data-dismiss="modal">Ok</button>
                </div>
            </div>

        </div>
    </div> 

    <div id="helpModal" data-backdrop="static" data-keyboard="false" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Game Rules</h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label style="color: Black;" id="msg">Hi folk, here are the simple rules to follow and win it..!!</label> 
                        <ul>
                            <li>Start/Join the game.</li>
                            <li>You will be assigned a random color.</li>
                            <li>Be fast and make the block yours by clicking it.</li>
                            <li>All the players will be blocked for a while.. be ready to color other blocks</li>
                            <li>In a go, only one block will be colored(it may or may not be yours)</li>
                            <li>You may or may not get the colored block, it's all about speed.</li>
                            <li>Winner will be the one with max number of colored blocks</li>
                        </ul>
                        <label style="color: Black;" id="msg">Speed is the game... Ace it.. Win it...</label> 

                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-warning" data-dismiss="modal">Gotcha!!!</button>
                </div>
            </div>

        </div>
    </div> 

    <div id="footer" style="bottom: 0px; position: absolute;"
         class="col-lg-12">
        <div class="col-lg-4">&nbsp;</div>
        <p align="right"><button type="button" class="btn btn-info btn-lg gameBtn"
                                                       data-toggle="modal" data-target="#helpModal">Help</button></p>
            </p></div>

    </div>
                
</body>
</html>