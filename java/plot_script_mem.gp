#set title "20% Insert, 30% Delete, 30% Find, KeyRange: 10^4" font "Times-Roman,25"
#set title "40% Insert, 30% Delete, 15% Find, 15% RQ, KeyRange: 10^6, RQSize: 2^{10}" font "Times-Roman,19"
#set title "40 Updaters (50% Insert, 50% Delete) and 40 RQers, KeyRange: 10^6" font "Times-Roman,20"
set title "0% Insert, 0% Delete, 100% Find, KeyRange: 10^6" font "Times-Roman,25"
set size square
set xlabel "Time (min)" font "Times-Roman,25" offset 0,-3
set ylabel "Allocated memory (GB)" font "Times-Roman,25" offset -6,0
set xrange [1:1116]
set yrange [0:120]
set lmargin 17
set rmargin 16
set bmargin 7
set tmargin 6
set key box at graph 0.5,0.9 font "Times-Roman,25" width 0.5 height 0.5
set xtics (0,"1" 60,"2" 120,"3" 180,"4" 240,"5" 300,"6" 360,"7" 420,"8" 480, "9" 540, "10" 600, "11" 660, "12" 720, "13" 780, "14" 840, "15" 900, "16" 960, "17" 1020, "18" 1080, "19" 1140) font "Times-Roman,25" offset -0.9,-1
set ytics font "Times-Roman,25"
set linestyle 1 lt 1 pt 70

#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 2 pt 7,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 2 pt 5,\

plot\
"toplot.txt" u 1:6 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 0 pt 5,\
"toplot.txt" u 1:4 t 'KIWI' w lp lw 3 lt rgb "#008000" ps 0 pt 7,\
"toplot.txt" u 1:5 t 'LFCA 64' w lp lw 3 lt rgb "#000000" ps 0 pt 1,\
"toplot.txt" u 1:2 t 'PNB-BST 16' w lp lw 3 lt rgb "#00d9ff" ps 0 pt 2,\
"toplot.txt" u 1:3 t 'PNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 0 pt 9
