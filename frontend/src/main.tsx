import React from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import App from "./App";
import NoteList from "./pages/NoteList";
import NoteDetail from "./pages/NoteDetail";
import NoteForm from "./pages/NoteForm";
import Login from "./pages/Login";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      { path: "/", element: <NoteList /> },
      { path: "/notes/new", element: <NoteForm /> },
      { path: "/notes/:id", element: <NoteDetail /> }
    ],
  },
]);

createRoot(document.getElementById("root")!).render(<RouterProvider router={router} />);