git co gh-pages
git merge master --no-edit
lein clean
lein cljsbuild once min
cp resources/public/js/compiled/app.js resources/public/js/compiled/prod.js
git add resources/public/js/compiled/prod.js -f
git commit -m "Update prod.js"
git push origin gh-pages
git co master
