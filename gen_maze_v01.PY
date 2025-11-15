import random
import tkinter as tk
from tkinter import filedialog, messagebox
from collections import Counter
from dataclasses import dataclass
from typing import List, Tuple, Set
import ttkbootstrap as ttk
from ttkbootstrap.constants import *

@dataclass
class MazeConfig:
    """Configuración del laberinto"""
    rows: int = 20
    cols: int = 20
    health: int = 10
    wall_prob: float = 0.3
    treasure_prob: float = 0.05
    ensure_path: bool = False
    
@dataclass
class CellColors:
    """Colores para cada tipo de celda"""
    WALL = "#d3d3d3"
    START = "#ffcdd2"
    END = "#ffcdd2"
    TREASURE = "#fff9c4"
    DEFAULT = "#e3f2fd"
    OUTLINE = "#cccccc"
    TEXT = "#333333"

# Constantes
MAX_CELL_SIZE = 40
MIN_CELL_SIZE = 5
DIGIT_WEIGHTS = [2**(9-i) for i in range(10)]
DIGIT_WEIGHTS_NORMALIZED = [w/sum(DIGIT_WEIGHTS) for w in DIGIT_WEIGHTS]

class MazeGeneratorApp(ttk.Window):
    """Aplicación para generar y visualizar laberintos"""
    
    def __init__(self):
        super().__init__(title="Generador MAZE - Zoom y Scroll", themename="cosmo")
        self.geometry("1000x700")
        self.resizable(True, True)
        
        self.config = MazeConfig()
        self.colors = CellColors()
        self.maze: List[List[str]] = []
        self.start: Tuple[int, int] = (0, 0)
        self.end: Tuple[int, int] = (0, 0)
        self.cell_size = 20
        
        self._create_widgets()
        self.generate_maze()

    def _create_widgets(self):
        """Crea todos los widgets de la interfaz"""
        self._create_control_panel()
        self._create_canvas_area()

    def _create_control_panel(self):
        """Crea el panel de controles"""
        frame = ttk.Frame(self, padding=10)
        frame.pack(side=LEFT, fill=Y)

        # Dimensiones
        self._add_labeled_entry(frame, "Filas (N)", self.config.rows, "n_var")
        self._add_labeled_entry(frame, "Columnas (M)", self.config.cols, "m_var")
        self._add_labeled_entry(frame, "Puntos de vida (P)", self.config.health, "p_var")

        # Probabilidades
        ttk.Label(frame, text="\nProbabilidades (0–1)").pack(anchor=W)
        self._add_labeled_entry(frame, "Pared (#)", self.config.wall_prob, "wall_prob", var_type="double")
        self._add_labeled_entry(frame, "Tesoro (T)", self.config.treasure_prob, "treasure_prob", var_type="double")

        # Checkbox
        self.ensure_path_var = ttk.BooleanVar(value=self.config.ensure_path)
        ttk.Checkbutton(
            frame, 
            text="Garantizar camino S → E", 
            variable=self.ensure_path_var,
            bootstyle="success-round-toggle"
        ).pack(anchor=W, pady=10)

        # Botones
        ttk.Button(
            frame, 
            text="Generar Laberinto", 
            bootstyle=SUCCESS, 
            command=self.generate_maze
        ).pack(fill=X, pady=5)
        
        ttk.Button(
            frame, 
            text="Guardar .TXT", 
            bootstyle=INFO, 
            command=self.save_maze
        ).pack(fill=X)

        # Tabla de frecuencias
        self._create_frequency_table(frame)

    def _add_labeled_entry(self, parent, label: str, value, var_name: str, var_type: str = "int"):
        """Helper para crear label + entry"""
        ttk.Label(parent, text=label).pack(anchor=W)
        var = ttk.DoubleVar(value=value) if var_type == "double" else ttk.IntVar(value=value)
        setattr(self, var_name, var)
        ttk.Entry(parent, textvariable=var, width=5).pack(anchor=W)

    def _create_frequency_table(self, parent):
        """Crea la tabla de frecuencias"""
        ttk.Label(parent, text="Frecuencia de caracteres").pack(anchor=W, pady=(5, 0))
        self.freq_table = ttk.Treeview(
            parent, 
            columns=("char", "count"),
            show="headings", 
            height=14
        )
        self.freq_table.heading("char", text="Carácter")
        self.freq_table.heading("count", text="Frecuencia")
        self.freq_table.column("char", width=80, anchor="center")
        self.freq_table.column("count", width=100, anchor="center")
        self.freq_table.pack(fill=X, pady=5)

    def _create_canvas_area(self):
        """Crea el área del canvas con scrollbars"""
        canvas_frame = ttk.Frame(self)
        canvas_frame.pack(side=RIGHT, fill=BOTH, expand=True)

        self.canvas = tk.Canvas(canvas_frame, bg="white")
        self.canvas.pack(side=LEFT, fill=BOTH, expand=True)

        # Scrollbars
        scroll_y = ttk.Scrollbar(canvas_frame, orient=VERTICAL, command=self.canvas.yview)
        scroll_y.pack(side=RIGHT, fill=Y)
        scroll_x = ttk.Scrollbar(self, orient=HORIZONTAL, command=self.canvas.xview)
        scroll_x.pack(side=BOTTOM, fill=X)

        self.canvas.configure(yscrollcommand=scroll_y.set, xscrollcommand=scroll_x.set)

        # Zoom bindings
        self.canvas.bind("<MouseWheel>", self._handle_zoom)
        self.canvas.bind("<Button-4>", self._handle_zoom)  # Linux
        self.canvas.bind("<Button-5>", self._handle_zoom)  # Linux

    def generate_maze(self):
        """Genera un nuevo laberinto"""
        rows, cols = self.n_var.get(), self.m_var.get()
        
        # Inicializa el laberinto vacío
        self.maze = [[' ' for _ in range(cols)] for _ in range(rows)]

        # Calcula tamaño de celda óptimo
        self.cell_size = self._calculate_cell_size(rows, cols)
        self.canvas.config(scrollregion=(0, 0, cols * self.cell_size, rows * self.cell_size))

        # Posiciona S y E
        self._place_start_end(rows, cols)

        # Llena el laberinto
        self._fill_maze(rows, cols)

        # Crea camino si es necesario
        if self.ensure_path_var.get():
            self._create_path()

        self._update_frequency_table()
        self._draw_maze()

    def _calculate_cell_size(self, rows: int, cols: int) -> float:
        """Calcula el tamaño óptimo de celda"""
        size = min(MAX_CELL_SIZE, 600 / cols, 600 / rows)
        return max(size, MIN_CELL_SIZE)

    def _place_start_end(self, rows: int, cols: int):
        """Coloca los puntos de inicio y fin"""
        positions = random.sample([(i, j) for i in range(rows) for j in range(cols)], 2)
        self.start, self.end = positions
        self.maze[self.start[0]][self.start[1]] = 'S'
        self.maze[self.end[0]][self.end[1]] = 'E'

    def _fill_maze(self, rows: int, cols: int):
        """Llena el laberinto con valores aleatorios"""
        special_positions = {self.start, self.end}
        
        for i in range(rows):
            for j in range(cols):
                if (i, j) not in special_positions:
                    self.maze[i][j] = self._random_cell()

    def _random_cell(self) -> str:
        """Genera un valor aleatorio para una celda"""
        # Paredes
        if random.random() < self.wall_prob.get():
            return '#'
        
        # Tesoros
        if random.random() < self.treasure_prob.get():
            return 'T'
        
        # Dígitos con distribución geométrica
        return random.choices('0123456789', weights=DIGIT_WEIGHTS_NORMALIZED)[0]

    def _create_path(self):
        """Crea un camino garantizado desde S hasta E"""
        rows, cols = len(self.maze), len(self.maze[0])
        i, j = self.start
        end_i, end_j = self.end

        visited: Set[Tuple[int, int]] = {(i, j)}
        directions = [(-1, 0), (1, 0), (0, -1), (0, 1)]

        while (i, j) != self.end:
            # Marca como parte del camino
            if (i, j) not in {self.start, self.end}:
                self.maze[i][j] = '0'

            # Busca movimiento válido
            random.shuffle(directions)
            moved = False
            
            for di, dj in directions:
                ni, nj = i + di, j + dj
                if (0 <= ni < rows and 0 <= nj < cols and 
                    (ni, nj) not in visited):
                    visited.add((ni, nj))
                    i, j = ni, nj
                    moved = True
                    break

            # Si no puede moverse, avanza hacia el objetivo
            if not moved:
                i += 1 if i < end_i else (-1 if i > end_i else 0)
                j += 1 if j < end_j else (-1 if j > end_j else 0)

        self.maze[end_i][end_j] = 'E'

    def _draw_maze(self):
        """Dibuja el laberinto en el canvas"""
        self.canvas.delete("all")
        rows, cols = len(self.maze), len(self.maze[0])
        cs = self.cell_size

        # Mapa de colores
        color_map = {
            '#': self.colors.WALL,
            'S': self.colors.START,
            'E': self.colors.END,
            'T': self.colors.TREASURE
        }

        for i in range(rows):
            for j in range(cols):
                x1, y1 = j * cs, i * cs
                x2, y2 = x1 + cs, y1 + cs
                
                cell = self.maze[i][j]
                color = color_map.get(cell, self.colors.DEFAULT)
                
                self.canvas.create_rectangle(
                    x1, y1, x2, y2, 
                    fill=color, 
                    outline=self.colors.OUTLINE
                )
                
                font_size = max(int(cs * 0.4), 6)
                self.canvas.create_text(
                    (x1 + x2) / 2, (y1 + y2) / 2, 
                    text=cell,
                    font=("Consolas", font_size, "bold"),
                    fill=self.colors.TEXT
                )

    def _handle_zoom(self, event):
        """Maneja el zoom con la rueda del mouse"""
        factor = 1.1 if (event.delta > 0 or event.num == 4) else 0.9
        new_size = self.cell_size * factor
        
        if MIN_CELL_SIZE <= new_size <= MAX_CELL_SIZE:
            self.cell_size = new_size
            cols = len(self.maze[0]) if self.maze else 0
            rows = len(self.maze) if self.maze else 0
            self.canvas.config(
                scrollregion=(0, 0, cols * self.cell_size, rows * self.cell_size)
            )
            self._draw_maze()

    def save_maze(self):
        """Guarda el laberinto en un archivo de texto"""
        if not self.maze:
            messagebox.showwarning("Advertencia", "Primero genera un laberinto.")
            return
        
        path = filedialog.asksaveasfilename(
            defaultextension=".txt", 
            filetypes=[("Text Files", "*.txt")]
        )
        
        if not path:
            return

        try:
            with open(path, "w", encoding="utf-8") as f:
                n, m, p = self.n_var.get(), self.m_var.get(), self.p_var.get()
                f.write(f"N={n}, M={m}, P={p}\n")
                f.writelines(f"{''.join(row)}\n" for row in self.maze)
            
            messagebox.showinfo("Éxito", f"Laberinto guardado en:\n{path}")
        except Exception as e:
            messagebox.showerror("Error", f"No se pudo guardar el archivo:\n{e}")

    def _update_frequency_table(self):
        """Actualiza la tabla de frecuencias"""
        if not self.maze:
            return

        # Cuenta frecuencias
        flat = (cell for row in self.maze for cell in row)
        freq = Counter(flat)

        # Limpia y llena la tabla
        for item in self.freq_table.get_children():
            self.freq_table.delete(item)

        for char, count in sorted(freq.items()):
            self.freq_table.insert("", "end", values=(char, count))


if __name__ == "__main__":
    app = MazeGeneratorApp()
    app.mainloop()