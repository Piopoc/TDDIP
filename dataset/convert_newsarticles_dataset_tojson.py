import csv
import json


def convert_csv_to_json(csv_file_path, json_file_path):
    output_f = open(json_file_path, 'w', encoding='utf-8')

    with open(csv_file_path, encoding='utf-8', errors='ignore') as input_f:
        # ignore errors; not a great solution but it is fine for the example
        csv_reader = csv.DictReader(input_f)

        for row in csv_reader:
            # each document has the following attributes: article_id,publish_date,article_source_link,title,subtitle,text

            if len(row["title"].strip()) == 0 or len(row["text"].strip()) == 0:
                continue

            text = row["title"] + " " + row["subtitle"] + " " + row["text"]

            document_length = len(text.split())  # approximation of the document length

            data = {
                "id": row["article_id"],
                "title": row["title"],
                "text": text,
                "date": row["publish_date"],
                "sources": ["News Articles dataset"],
                "url": row["article_source_link"],
                "documentLength": document_length
            }

            output_f.write(json.dumps(data) + "\n")

    output_f.close()


# dataset downloaded from here: https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/GMFCTR
csv_file_path = "data/NewsArticles.csv"

json_file_path = "data/mediabias_newsarticles.jsonl"

convert_csv_to_json(csv_file_path, json_file_path)
