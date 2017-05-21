// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
//= require /webjars/jquery/2.2.4/jquery.min
//= require /webjars/bootstrap/3.3.7-1/js/bootstrap.min
//= require /webjars/datatables/1.10.13/js/jquery.dataTables.min
//= require searchHighlight/jquery.highlight
//= require searchHighlight/dataTables.searchHighlight.min
//= require /webjars/datatables.net-scroller/1.4.2/js/dataTables.scroller.min
//= require_tree .
//= require_self

if (typeof jQuery !== 'undefined') {
    (function($) {
        $('#spinner').ajaxStart(function() {
            $(this).fadeIn();
        }).ajaxStop(function() {
            $(this).fadeOut();
        });
    })(jQuery);
}
