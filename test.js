

function loopChecker(ans, times){

    for (var i=0; i< times; i++){

        ans = ans * ans;
    }

    console.log(ans);
}

var ans = 5;
var times = 3;
loopChecker(ans, times);