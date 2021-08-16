package org.unyw



val svgBanner = """
<svg id="svg8" version="1.1" viewBox="0 0 32.921 15.009" xmlns="http://www.w3.org/2000/svg" xmlns:cc="http://creativecommons.org/ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
 <metadata id="metadata5">
  <rdf:RDF>
   <cc:Work rdf:about="">
    <dc:format>image/svg+xml</dc:format>
    <dc:type rdf:resource="http://purl.org/dc/dcmitype/StillImage"/>
    <dc:title/>
   </cc:Work>
  </rdf:RDF>
 </metadata>
 <g id="layer4" transform="translate(-59.604 -40.355)">
  <path id="rect96" d="m59.604 40.355v14.773a16.47 7.3534 0 0 1 16.46-7.2673 16.47 7.3534 0 0 1 16.461 7.2321v-14.738zm32.921 14.946a16.47 7.3534 0 0 1-7e-3 0.0629h7e-3zm-32.921 0.03665v0.02626h0.0021a16.47 7.3534 0 0 1-0.0021-0.02626z" fill="#1565c0" stroke-width=".19996"/>
  <path id="path2396" d="m76.065 49.236-3.0711 1.5055 3.1398 3.3178 3.0024-3.3178z" fill="#c69100" stroke-width="0" style="paint-order:normal"/>
  <path id="path900" d="m72.403 43.336a2.7952 5.2595 0 0 0-2.3418 2.4539l0.32184 0.38178a2.7952 6.2496 0 0 1 2.0661-1.9892 2.7952 6.2496 0 0 1 2.0437 2.0497l0.32184-0.36861a2.7952 5.2595 0 0 0-2.3655-2.5276 2.7952 5.2595 0 0 0-0.04618 0z" fill="#fff" stroke="#fff" stroke-linecap="round" stroke-linejoin="bevel" stroke-width=".45824" style="paint-order:normal"/>
  <path id="path919" d="m79.52 43.346a2.7952 5.2595 0 0 1 2.3418 2.4539l-0.32184 0.38178a2.7952 6.2496 0 0 0-2.0661-1.9892 2.7952 6.2496 0 0 0-2.0437 2.0497l-0.32184-0.36861a2.7952 5.2595 0 0 1 2.3655-2.5276 2.7952 5.2595 0 0 1 0.04618 0z" fill="#fff" stroke="#fff" stroke-linecap="round" stroke-linejoin="bevel" stroke-width=".45824" style="paint-order:normal"/>
 </g>
</svg>
""".trimIndent()


val svgBannerError = """
<svg id="svg8" version="1.1" viewBox="0 0 32.921 15.009" xmlns="http://www.w3.org/2000/svg" xmlns:cc="http://creativecommons.org/ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
 <metadata id="metadata5">
  <rdf:RDF>
   <cc:Work rdf:about="">
    <dc:format>image/svg+xml</dc:format>
    <dc:type rdf:resource="http://purl.org/dc/dcmitype/StillImage"/>
    <dc:title/>
   </cc:Work>
  </rdf:RDF>
 </metadata>
 <g id="layer4" transform="translate(-59.604 -40.355)">
  <path id="rect96" d="m59.604 40.355v14.773a16.47 7.3534 0 0 1 16.46-7.2673 16.47 7.3534 0 0 1 16.461 7.2321v-14.738zm32.921 14.946a16.47 7.3534 0 0 1-7e-3 0.0629h7e-3zm-32.921 0.03665v0.02626h0.0021a16.47 7.3534 0 0 1-0.0021-0.02626z" fill="#1565c0" stroke-width=".19996"/>
  <path id="path2396" d="m76.065 49.236-3.0711 1.5055 3.1398 3.3178 3.0024-3.3178z" fill="#c69100" stroke-width="0" style="paint-order:normal"/>
  <rect id="rect883" transform="rotate(45)" x="83.024" y="-21.471" width=".84038" height="3.2775" fill="#fff" stroke-width=".26802"/>
  <rect id="rect883-3" transform="rotate(-45)" x="19.412" y="81.806" width=".84038" height="3.2775" fill="#fff" stroke-width=".26802"/>
  <rect id="rect883-7" transform="rotate(45)" x="87.179" y="-25.615" width=".84038" height="3.2775" fill="#fff" stroke-width=".26802"/>
  <rect id="rect883-3-5" transform="rotate(-45)" x="23.556" y="85.961" width=".84038" height="3.2775" fill="#fff" stroke-width=".26802"/>
 </g>
</svg>

""".trimIndent()

// https://codepen.io/finnhvman/pen/OBLZRX
val cssProgressBar = """
    .pure-material-progress-linear {
        -webkit-appearance: none;
        -moz-appearance: none;
        appearance: none;
        border: none;
        height: 0.25em;
        color: rgb(var(--pure-material-primary-rgb, 33, 150, 243));
        background-color: rgba(var(--pure-material-primary-rgb, 33, 150, 243), 0.12);
        font-size: 16px;
    }

    .pure-material-progress-linear::-webkit-progress-bar {
        background-color: transparent;
    }

    /* Determinate */
    .pure-material-progress-linear::-webkit-progress-value {
        background-color: currentColor;
        transition: all 0.2s;
    }

    .pure-material-progress-linear::-moz-progress-bar {
        background-color: currentColor;
        transition: all 0.2s;
    }

    .pure-material-progress-linear::-ms-fill {
        border: none;
        background-color: currentColor;
        transition: all 0.2s;
    }

    /* Indeterminate */
    .pure-material-progress-linear:indeterminate {
        background-size: 200% 100%;
        background-image: linear-gradient(to right, transparent 50%, currentColor 50%, currentColor 60%, transparent 60%, transparent 71.5%, currentColor 71.5%, currentColor 84%, transparent 84%);
        animation: pure-material-progress-linear 2s infinite linear;
    }

    .pure-material-progress-linear:indeterminate::-moz-progress-bar {
        background-color: transparent;
    }

    .pure-material-progress-linear:indeterminate::-ms-fill {
        animation-name: none;
    }

    @keyframes pure-material-progress-linear {
        0% {
            background-size: 200% 100%;
            background-position: left -31.25% top 0%;
        }
        50% {
            background-size: 800% 100%;
            background-position: left -49% top 0%;
        }
        100% {
            background-size: 400% 100%;
            background-position: left -102% top 0%;
        }
    }
""".trimIndent()


val PageEmpty = """
    <style>
        html, body { margin: 0; padding: 0; }
    </style>
    $svgBanner
""".trimIndent()

val PageInstall = """
     <style>
        $cssProgressBar
        body { margin: 0; padding: 0; user-select: none; }
        progress {margin-bottom: 20px; }
        #output { padding: 10px; }
        #output p{ margin: 0; padding: 0; overflow-y: auto; font-size: 1.2rem; }
        h1 { text-align: center; width: 100%; }
     </style>
     $svgBanner
     <h1>Installing</h1>
     <progress class="pure-material-progress-linear" style="width: 100%"></progress>
     <div id="output"></div>
     <script>
         const p = document.getElementById('output')
         window.installTitle = s => p.innerHTML += '<p style="margin: 10px 0;"><b>' + s + '</b></p>'
         window.installLog   = s => p.innerHTML += '<p>' + s + '</p>'
         window.installError = s => p.innerHTML += '<p style="color:red">' + s + '</p>' 
     </script>
""".trimIndent()


val Page404 = {page : String ->  """
    <style>
        html, body {margin: 0; padding: 0; }
        * {box-sizing: border-box; }
        h1 {text-align: center; width: 100%; margin: 0; margin-top: 30px; }
        p { text-align: center; margin-bottom: 40px; margin-top: 10px;}
        #links { padding: 10px; }
        a { width: 100%; padding: 15px; border-radius: 15px; background-color: #eee; display: block; }
    </style>
    $svgBannerError
    <h1>Page not found</h1>
    <p><b>Requested:</b> “$page”</p>
    <div id="links">
        <a href="/apps/home/index.html">Go to home</a>
    </div>
""".trimIndent()}

// https://www.w3schools.com/howto/howto_css_loader.asp
val PageSplashscreen = """
    <!DOCTYPE html>
    <html>
    <head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
    .loader {
      border: 10px solid #f3f3f3;
      border-radius: 50%;
      border-top: 10px solid #3498db;
      width: 80px;
      height: 80px;
      animation: spin 2s linear infinite;
      margin: 30vh auto;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    </style>
    </head>
    <body>
    <div class="loader"></div>
    <script>
    setTimeout( () => location.replace(location.hash.substr(1)), 1000)
    </script>
    </body>
    </html>
""".trimIndent()