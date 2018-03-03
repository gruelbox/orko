export default function fetchData(url, callback) {
    fetch(new Request('http://localhost:8080/api/' + url, {
        method: 'GET', 
        mode: 'cors', 
        redirect: 'follow',
        credentials: 'include',
        headers: new Headers({
            "Authorization": "Basic YnVsbHk6Ym95",
            "Content-type": "application/json"
        })
    }))
    .then(response => response.json())
    .then(callback)
    .catch(err => {
        console.log('Fetch Error :-S', err);
        callback(undefined);
    });
}