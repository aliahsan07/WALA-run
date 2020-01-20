

function loopChecker(ans, times){

    for (var i=0; i< times; i++){

        ans = ans * ans;
    }

    console.log(ans);
}

var ans = 5;
ans = ans + 2;
var times = 9;
loopChecker(ans, times);