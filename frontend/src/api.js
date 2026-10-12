import axios from "axios";

// All pages talk to the backend through this one object.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
});

// Gets a readable message out of an error.
export function errMsg(e) {
  return e.response?.data?.message || "Could not reach the backend. Is it running?";
}

export default api;
