(() => {
  'use strict';
  const files = ['app.part01.txt', 'app.part02.txt', 'app.part03.txt', 'app.part04.txt', 'app.part05.txt', 'app.part06.txt', 'app.part07.txt'];
  let source = '';
  for (const file of files) {
    const request = new XMLHttpRequest();
    request.open('GET', file, false);
    request.send(null);
    if (request.status < 200 || request.status >= 300) {
      throw new Error(`تعذر تحميل جزء التطبيق: ${file}`);
    }
    source += request.responseText;
  }
  (0, eval)(source);
})();
