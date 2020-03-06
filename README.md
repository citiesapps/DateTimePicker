# Welcome to our custom DatePicker/CircleListView ;)

The need for a custom DatePicker with an iOS style like appearance emerged due to requirement of inputting their birthdate with year. The default android DatePicker was too complicated, other libraries offered to pick years in an additional dialog. Both options were subpar. Therefore a custom one needed to be made.

The DatePicker is just a DialogFragment that is based on the custom view CircleListView, which simulates its items to appear on a circle.


### Adding the DatePicker as a submodule to another project
Inside the other project's root directory

    git submodule add git@gitlab.com:devappmea/intern/datepicker.git
    
Inside the projects root directory modify the `gradle.settings` file

    include 'YOUR_MAIN_MODULE', ':datepicker:library'
    
Finally include the submodule to the projects gradle dependency tree inside your YOUR_MAIN_MODULE's gradle.build file

    dependencies {
        ...
        implementation project(path: ':datepicker:library')
        ...
    }

