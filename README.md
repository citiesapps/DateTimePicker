# Welcome to our custom DatePicker/CircleListView ;)

The need for a custom DatePicker with an iOS style like appearance emerged due to requirement of inputting their birthdate with year. The default android DatePicker was too complicated, other libraries offered to pick years in an additional dialog. Both options were subpar. Therefore a custom one needed to be made.

The DatePicker is just a DialogFragment that is based on the custom view CircleListView, which simulates its items to appear on a circle.



test

## Adding the DatePicker as a submodule to another project
Inside the other project's root directory

    git submodule add git@gitlab.com:devappmea/intern/datepicker.git
    
Inside the projects root directory modify the `gradle.settings` file

    include 'YOUR_MAIN_MODULE', ':datepicker:library'
    
Finally update your YOUR_MAIN_MODULE's gradle.build file

    android {
        ...
    
        compileOptions {
            ...
            targetCompatibility = 1.8
            sourceCompatibility = 1.8
            ...
        }
        
        ...
        
        defaultConfig {
            ...
            minSdkVersion 21
            ...
        }
    }
    
    ... 
    
    dependencies {
        ...
        implementation project(path: ':datepicker:library')
        ...
    }
    
## Usage of Date-/TimePickerDialogFragment
1. You need to implement the `DateSelectListener` interface inside your calling Activity/Fragment, to support automatic recreation when changing rotation etc.

2. Just call the below method from any Activity/Fragment you want  

        DatePickerDialogFragment.startFragment(
            this, // this == either the Activity or Fragment
            new DatePickerDialogFragment.Builder()
                // add possible options to the Builder like textSize, coloring, custom text, ...
                // See the corresponding classes for possible options
        );
    
        TimePickerDialogFragment.startFragment(
            this, // this == either the Activity or Fragment
            new TimePickerDialogFragment.Builder()
                // add possible options to the Builder like textSize, coloring, custom text, ...
                // See the corresponding classes for possible options
        );





## Working with submodules


### Updating submodules after changes to the submodule repo were made
To pull the newest changes from submodules master including submodule's submodules   

**IMPORTANT:** When using this method to update the submodule, the cheched-out state will be **DETACHED-HEAD**. So any local changes will be overwritten when using the command again. Make sure to check out a branch when doing changes locally.

    git submodule update --recursive --remote




### Working on submodules inside including project
As already mentioned you can work on files of the submodule from within the project that includes the submodule. As the above method will result in a DETACHED-HEAD state you need to checkout a branch. You can do so by using AndroidStudio's built in git feature to checkout a branch. The popup will now have different repositories: 1 for your project, 1 for each submodule you imported. You can there select a branch or use the commands below to do the same
    
    cd your_submodules_name
    git checkout your_branch_name
    
You can then work with your submodule like with any other repo, make changes, push them, ...
