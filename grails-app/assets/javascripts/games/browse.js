//
// Delete a ROM.
//
$(document).on('click', 'button.deleteRom', function (e) {
    e.preventDefault();

    var button = $(this);
    var deleteUrl = button.data('delete-url');
    if (deleteUrl) {
        button.css("background", "yellow");
        button.html('Queueing to Move ROM to Trash');
        $.ajax({
            url: deleteUrl,
            type: 'DELETE',
            success: function (result) {
                button.html('ROM Queued to Move To Trash');
                button.prop('disabled', true);
                button.css("background", "chartreuse");
            },
            error: function (result) {
                button.css("background", "");
                button.html('Delete / Move Trash');
                alert('Error deleting the ROM.');
            }
        });
    }
});

//
// Toggle the visibility of a column by clicking on the field name
// at the top of the page.
//
$(document).on('click', 'a.toggle-vis', function (e) {
    e.preventDefault();

    // Get the column API object
    var column = romTable.column( $(this).attr('data-column') );

    // Toggle the visibility
    column.visible( ! column.visible() );
});

//
// User is deleting all matching roms. Copy the query to the
// forms hidden query field.
//
$(document).on('click', '#deleteAllMatching', function (e) {
    var query = $("#romTable_filter>label>input").val();
    if (query) {
        $("#deleteAllQuery").val(query);
    }
    else {
        alert("No query specified.");
        event.preventDefault()
    }
});
