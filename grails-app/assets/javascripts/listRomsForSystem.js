$(document).ready(function() {
    $('#romTable').DataTable({
        searchHighlight: true,
        aLengthMenu: [
            [-1],
            ["All"]
        ],
        iDisplayLength: -1,
        aoColumnDefs: [
            {
                // Custom rendering for the first column
                // which is filename over scrape name
                "mRender": function ( data, type, row ) {
                    if (row[1]) {
                        return row[1] + '<br/>' + data;
                    }
                    else {
                        return data;
                }
                },
                "aTargets": [ 0 ]
            },
            { "bVisible": false,  "aTargets": [ 1 ] }
        ],
         "columns": [
            { "width": "35%" },
            { "width": "0%" },
            { "width": "5%" },
            { "width": "40%" },
            { "width": "20%" }
        ],
        "paging":   false
    });
});

$(document).on('click', '.deleteRom', function () {
    var button = $(this);
    var deleteUrl = button.data('delete-url');
    if (deleteUrl) {
        button.css("background", "yellow");
        button.attr('value', 'Attempting to Move ROM to Trash');
        $.ajax({
            url: deleteUrl,
            type: 'DELETE',
            success: function (result) {
                button.attr('value', 'ROM Moved To Trash');
                button.prop('disabled', true);
                button.css("background", "green");
                console.log("ROM deleted");
            },
            error: function (result) {
                button.css("background", "");
                button.attr('value', 'Delete / Move Trash');
                alert('Error deleting the ROM.');
            }
        });
    }
});