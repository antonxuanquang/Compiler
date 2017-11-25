var gulp  = require('gulp')
var shell = require('gulp-shell')
var watch = require('gulp-watch')

gulp.task('compile', shell.task([
	'clear',
	// 'javacc ./Grammar.jj',
	// 'javac ./Grammar.java',
	'java ./Grammar < ./TestCases/PL40603Test.txt'
]))

gulp.task('watch', function() {
	gulp.watch('./*.jj', ['compile']);
	gulp.watch('./*.java', ['compile']);
});

gulp.task('default' ,function() {
	gulp.watch('./*.jj', ['compile']);
	gulp.watch('./*.java', ['compile']);
});
