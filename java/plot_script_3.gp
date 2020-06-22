set title "20% Insert, 10% Delete, 70% Find  |  Number of Threads: 32  |  No GC" font "Times-Roman,13"
set xlabel "Approx. tree size in steady state" font "Times-Roman,13"
set ylabel "Throughput (ops/μs)" font "Times-Roman,13"
set xrange [30:8000000]
set yrange [0:90]
set key box at 2000000, 80 font "Helvetica, 18" width 1.5 height 0.5
set xtics("6x10^1" 60, "6x10^2" 600, "6x10^3" 6000, "6x10^4" 60000, "6x10^5" 600000, "6x10^6" 6000000) set xtics
set ytics font "Times-Roman,13"
set logscale x 10
set linestyle 1 lt 1 pt 70

plot\
"toplot.txt" u 1:2 t 'BST' w lp lw 3 lt 1 ps 2 pt 7,\
"toplot.txt" u 1:3 t 'PBST' w lp lw 3 lt 2 ps 1.9 pt 5
