<%@include file="/apps/groovyconsole/components/global.jsp" %>

<div class="btn-toolbar">
    <div class="btn-group">
        <a class="btn btn-success" href="#" id="run-script">
            <i class="icon-play icon-white"></i> <span id="run-script-text">Run Script</span>
        </a>
        <a class="btn btn-success" href="#" id="dry-run-script">
            <i class="icon-play-circle icon-white"></i> <span id="dry-run-script-text">Dry Run Script</span>
        </a>
    </div>

    <div class="btn-group">
        <a class="btn" href="#" id="new-script"><i class="icon-pencil"></i> New</a>

        <c:if test="${isAuthor}">
        	<a class="btn" href="#" id="open-script"><i class="icon-folder-open"></i> Open</a>
        	<a class="btn" href="#" id="save-script"><i class="icon-hdd"></i> Save</a>
        </c:if>
    </div>

    <div id="loader">
        <img src="/etc/groovyconsole/clientlibs/img/ajax-loader.gif">
    </div>

    <div id="btn-group-services" class="btn-group pull-right">
        <input id="services-list" type="text" placeholder="Service or Adapter Name">
    </div>
</div>